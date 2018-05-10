package com.jaumard.rxwearbridge

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import java.io.ByteArrayOutputStream
import java.security.InvalidParameterException

internal fun <T> Task<T>.toSingle(): Single<T> {
    return Single.defer { Single.just(Tasks.await(this)) }
}

class RxWearBridge(private val context: Context,
                   val messageSubject: PublishSubject<MessageEvent> = PublishSubject.create(),
                   val dataSubject: PublishSubject<Pair<String, DataMap>> = PublishSubject.create(),
                   val capabilitySubject: PublishSubject<CapabilityInfo> = PublishSubject.create()) : DataClient.OnDataChangedListener,
        MessageClient.OnMessageReceivedListener, CapabilityClient.OnCapabilityChangedListener {

    companion object {
        private const val DATA_ARRAY = "data-array"
        private const val DATA_ITEM = "data-item"
        private const val DATA_ASSET = "data-asset"
        private const val TAG = "RxWearBridge"
    }

    /**
     * Bind capabilities by adding a listener how will notify {@link capabilitySubject} per capability that a device has been detected
     * @param capabilities list of capabilities to bind as String
     */
    fun bindCapability(vararg capabilities: String) {
        capabilities.forEach {
            Wearable.getCapabilityClient(context).addListener(this, it)
        }
    }

    /**
     * Bind capabilities by adding a listener how will notify {@see capabilitySubject} per capability that a device has been detected
     * @param data list of capabilities as Pair<Uri, Int>
     */
    fun bindCapability(vararg data: Pair<Uri, Int>) {
        data.forEach { (uri, filterType) ->
            Wearable.getCapabilityClient(context).addListener(this, uri, filterType)
        }
    }

    /**
     * Bind data and messages to notify {@see dataSubject} and {@see messageSubject} when new data or messages append
     */
    fun bind() {
        Wearable.getDataClient(context).addListener(this)
        Wearable.getMessageClient(context).addListener(this)
    }

    /**
     * Unbind capabilities by there names
     * @param capabilities list of capabilities as String to unbind
     */
    fun unbindCapability(vararg capabilities: String) {
        capabilities.forEach {
            Wearable.getCapabilityClient(context).removeListener(this, it)
        }
    }

    /**
     * Unbind data, messages and capabilities added with Uri
     */
    fun unbind() {
        Wearable.getDataClient(context).removeListener(this)
        Wearable.getCapabilityClient(context).removeListener(this)
        Wearable.getMessageClient(context).removeListener(this)
    }

    /**
     * @param path of the message as String
     * @param dataMap optional data to send with the message, default empty
     * @param capability optional String to specify a capability to target when sending the message, default send to all nodes
     * @return Completable who complete once message sent or fail
     */
    fun sendMessage(path: String, dataMap: DataMap = DataMap(), capability: String? = null): Completable {
        return getNodes(capability)
                .flattenAsObservable { it }
                .flatMapSingle {
                    val sendMessageTask = Wearable.getMessageClient(context).sendMessage(it, path, dataMap.toByteArray())
                    sendMessageTask.toSingle()
                }.ignoreElements()
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        capabilitySubject.onNext(capabilityInfo)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            val dataMap = DataMap.fromByteArray(event.dataItem.data)
            when {
                dataMap.containsKey(DATA_ITEM) -> dataSubject.onNext(Pair(event.dataItem.uri.path, dataMap.getDataMap(DATA_ITEM)))
                dataMap.containsKey(DATA_ARRAY) -> {
                    dataMap.getDataMapArrayList(DATA_ARRAY).forEach { data ->
                        dataSubject.onNext(Pair(event.dataItem.uri.path, data))
                    }
                }
                else -> dataSubject.onNext(Pair(event.dataItem.uri.path, dataMap))
            }

        }
        dataEvents.release()
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        messageSubject.onNext(messageEvent)
    }

    /**
     * Sync data for a given path
     * @param path where to sync the data
     * @param dataMap DataMap to sync
     * @return Single of DataItem containing the sync data
     */
    fun syncData(path: String, dataMap: DataMap): Single<DataItem> {
        return Single.defer {
            val putDataMapRequest = PutDataMapRequest.create(path)
            putDataMapRequest.dataMap.putDataMap(DATA_ITEM, dataMap)
            val request = putDataMapRequest.asPutDataRequest()
            val putDataItem = Wearable.getDataClient(context).putDataItem(request)
            putDataItem.toSingle()
        }
    }

    /**
     * Sync multiple DataMap at one for a given path
     * @param path where to sync the data
     * @param dataMaps ArrayList of DataMap to sync
     * @return Single of DataItem containing the sync data
     */
    fun syncDataArray(path: String, dataMaps: ArrayList<DataMap>): Single<DataItem> {
        return Single.defer {
            val putDataMapRequest = PutDataMapRequest.create(path)
            putDataMapRequest.dataMap.putDataMapArrayList(DATA_ARRAY, dataMaps)
            val request = putDataMapRequest.asPutDataRequest()
            val putDataItem = Wearable.getDataClient(context).putDataItem(request)
            putDataItem.toSingle()
        }
    }

    /**
     * Get multiple data from Uri, if Uri not present, all data it returned
     * @param path where to retrieve the data, optional String
     * @param locally should the data by retrieve on the current device
     * @return Single of list of DataMap containing the sync data
     */
    fun getDataArray(path: String, locally: Boolean = false): Single<List<DataMap>> {
        return if (locally) {
            getLocalNodeId().flatMap {
                val uri = Uri.Builder()
                        .scheme(PutDataRequest.WEAR_URI_SCHEME)
                        .path(path)
                        .authority(it)
                        .build()
                Log.d(TAG, "getData $uri")
                Wearable.getDataClient(context).getDataItem(uri).toSingle()
            }
        } else {
            getNodes().flatMap {
                val uri = Uri.Builder()
                        .scheme(PutDataRequest.WEAR_URI_SCHEME)
                        .path(path)
                        .authority(it.last())
                        .build()
                Log.d(TAG, "getData $uri")
                Wearable.getDataClient(context).getDataItem(uri).toSingle()
            }
        }.map { DataMap.fromByteArray(it.data).getDataMapArrayList(DATA_ARRAY) }
    }

    /**
     * @param path where to retrieve the data, optional String
     * @return Single of list of DataMap containing all the data
     */
    fun getAllData(path: String? = null): Single<List<DataMap>> {
        return if (path == null) {
            Log.d(TAG, "getAllData")
            Wearable.getDataClient(context).dataItems.toSingle()
        } else {
            val uri = Uri.Builder()
                    .scheme(PutDataRequest.WEAR_URI_SCHEME)
                    .path(path)
                    .build()
            Log.d(TAG, "getAllData for $uri")
            Wearable.getDataClient(context).getDataItems(uri).toSingle()
        }.map { buffer ->
            buffer.map { DataMapItem.fromDataItem(it).dataMap }
                    .toList()
                    .also {
                        buffer.release()
                    }
        }
    }

    private fun getRawDataMap(path: String, locally: Boolean = false): Single<DataMap> {
        return if (locally) {
            getLocalNodeId().flatMap {
                val uri = Uri.Builder()
                        .scheme(PutDataRequest.WEAR_URI_SCHEME)
                        .path(path)
                        .authority(it)
                        .build()
                Log.d(TAG, "getRawDataMap for $uri")
                Wearable.getDataClient(context).getDataItem(uri).toSingle()
            }
        } else {
            getNodes().flatMap {
                val uri = Uri.Builder()
                        .scheme(PutDataRequest.WEAR_URI_SCHEME)
                        .path(path)
                        .authority(it.last())
                        .build()
                Log.e("TAG", "getRawDataMap for $uri")
                Wearable.getDataClient(context).getDataItem(uri).toSingle()
            }
        }.map { DataMapItem.fromDataItem(it).dataMap }
    }

    /**
     * Get specific data from path
     * @param path where retrieve the data as Uri
     * @param locally should the data by retrieve on the current device
     * @return Single of DataMap containing the sync data
     */
    fun getData(path: String, locally: Boolean = false): Single<DataMap> {
        return getRawDataMap(path, locally).map { it.getDataMap(DATA_ITEM) }
    }

    /**
     * Sync a bitmap on data layer
     * @param path to the bitmap to sync as String
     * @param assetName name given to the bitmap to sync
     * @param bitmap to sync
     * @return Single of DataItem containing the sync bitmap
     */
    fun syncBitmap(path: String, assetName: String, bitmap: Bitmap): Single<DataItem> {
        return Single.defer {
            val asset = createAssetFromBitmap(bitmap)
            if (asset != null) {
                val dataMapRequest = PutDataMapRequest.create("$path/$assetName")
                dataMapRequest.dataMap.putAsset(assetName, asset)
                val request = dataMapRequest.asPutDataRequest()
                Wearable.getDataClient(context).putDataItem(request).toSingle()
            } else {
                Single.error(InvalidParameterException("syncBitmap(): asset from bitmap is null"))
            }
        }
    }

    /**
     * Retrieve a sync bitmap
     * @param path to the bitmap to retrieve as String
     * @param assetName name given to the bitmap to retrieve
     * @return Single of Bitmap
     */
    fun getBitmap(path: String, assetName: String): Single<Bitmap> {
        return getRawDataMap("$path/$assetName")
                .flatMap {
                    val asset = it.getAsset(assetName)
                    loadBitmapFromAsset(asset)
                }
    }

    private fun getLocalNodeId(): Single<String> {
        return Wearable.getNodeClient(context).localNode.toSingle().map { it.id }
    }

    private fun createAssetFromBitmap(bitmap: Bitmap?): Asset? {
        if (bitmap != null) {
            val byteStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream)
            return Asset.createFromBytes(byteStream.toByteArray())
        }
        return null
    }

    private fun loadBitmapFromAsset(asset: Asset): Single<Bitmap> {
        return Single.defer {
            val task = Wearable.getDataClient(context).getFdForAsset(asset)
            task.toSingle().map {
                BitmapFactory.decodeStream(it.inputStream)
            }
        }
    }

    private fun getNodes(capability: String? = null): Single<List<String>> {
        return if (capability == null) {
            Wearable.getNodeClient(context).connectedNodes.toSingle()
                    .flattenAsObservable { it }
        } else {
            Wearable.getCapabilityClient(context).getCapability(capability, CapabilityClient.FILTER_REACHABLE).toSingle()
                    .flattenAsObservable { it.nodes }
        }
                .filter { it.isNearby }
                .map { it.id }
                .toList()
    }

}