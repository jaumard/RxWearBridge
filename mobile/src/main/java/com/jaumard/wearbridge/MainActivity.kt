package com.jaumard.wearbridge

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.android.gms.wearable.DataMap
import com.jaumard.common.Contants
import com.jaumard.common.Contants.Companion.BITMAP_KEY
import com.jaumard.common.Contants.Companion.BITMAP_PATH
import com.jaumard.common.Contants.Companion.DATA_INT_KEY
import com.jaumard.common.Contants.Companion.DATA_KEY
import com.jaumard.rxwearbridge.RxWearBridge
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "RxWearBridge"
    }

    private val rxWearBridge = RxWearBridge(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rxWearBridge.messageSubject.subscribe {
            message.text = "${message.text}\n${it.path} from ${it.sourceNodeId}"
            Log.d(TAG, "${it.path} from ${it.sourceNodeId}")
        }

        syncData()
        syncDataArray()
        syncBitmap()
    }

    private fun syncBitmap() {
        val bitmapIcon = BitmapFactory.decodeResource(resources, R.drawable.profil)
        rxWearBridge.syncBitmap(BITMAP_PATH, BITMAP_KEY, bitmapIcon)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onError = {
                    Log.e(TAG, "syncBitmap", it)
                }, onSuccess = {
                    Log.i(TAG, "syncBitmap ok $it")
                })
    }

    private fun syncDataArray() {
        val dataMap = DataMap()
        dataMap.putString(DATA_KEY, "myDataArraySync")
        dataMap.putInt(DATA_INT_KEY, 4)
        val dataMap2 = DataMap()
        dataMap2.putString(DATA_KEY, "myDataArraySync2")
        dataMap2.putInt(DATA_INT_KEY, 5)
        val dataMapArray = arrayListOf(dataMap, dataMap2)
        rxWearBridge.syncDataArray(Contants.DATA_ARRAY_PATH, dataMapArray)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onError = {
                    Log.e(TAG, "syncDataArray", it)
                }, onSuccess = {
                    Log.i(TAG, "syncDataArray ok $it")
                })
    }

    private fun syncData() {
        val dataMap = DataMap()
        dataMap.putString(DATA_KEY, "myDataSync")
        dataMap.putInt(DATA_INT_KEY, 9)
        rxWearBridge.syncData(Contants.DATA_PATH, dataMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onError = {
                    Log.e(TAG, "syncData", it)
                }, onSuccess = {
                    Log.i(TAG, "syncData ok $it")
                })
    }

    fun updateData(v: View) {
        val dataMap = DataMap()
        dataMap.putString(DATA_KEY, "myDataSyncUpdated")
        dataMap.putInt(DATA_INT_KEY, Math.round(Math.random() * 100).toInt())
        rxWearBridge.syncData(Contants.DATA_PATH, dataMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onError = {
                    Log.e(TAG, "updateData", it)
                }, onSuccess = {
                    Log.i(TAG, "updateData ok $it")
                })
    }

    fun sendMessage(v: View) {
        rxWearBridge.sendMessage(Contants.MESSAGE_PATH)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onError = {
                    Log.e(TAG, "sendMessage", it)
                }, onComplete = {
                    Log.i(TAG, "sendMessage ok")
                })
    }

    override fun onStart() {
        super.onStart()
        rxWearBridge.bind()
    }

    override fun onStop() {
        super.onStop()
        rxWearBridge.unbind()
    }
}
