# RxWearBridge

[![Download](https://api.bintray.com/packages/jaumard/maven/rxwearbridge/images/download.svg)](https://bintray.com/jaumard/maven/rxwearbridge/_latestVersion)

Small library to support data synchronisation and messages between Android and Android Wear devices with Rx interface

It's easy to use, first install the library:

```groovy
implementation 'com.jaumard:rxwearbridge:x.x.x'
```

Under your main build gradle you need to add this:

```groovy
allprojects {
    repositories {
        maven { url "http://dl.bintray.com/jaumard/maven" }
        //...other repo here
    }
}
```

## Basic usage
What can I do with this library ?

You can send and recieve messages, data, assets. You can listen for new capability on the wear network and send messages per capability

### Foreground usage (Activity/Fragment/ViewModel...)
```
class MainActivity : AppCompatActivity() {
    private val rxWearBridge = RxWearBridge(this)

    fun sendMessage() {
        rxWearBridge.sendMessage(MESSAGE_PATH, DataMap().apply{ putInt("test", 5) })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onError = {
                    //an error occured
                }, onComplete = {
                    //message sent
                })
    }

    fun listenForMessage() {
        //don't forget to dispose it later
        rxWearBridge.messageSubject.subscribe {
            Log.d(TAG, "${it.path} from ${it.sourceNodeId}")
        }
    }

    fun listenForCapability() {
        //don't forget to dispose it later
        rxWearBridge.capabilitySubject.subscribe { (path, dataMap) ->
            Log.d(TAG, "${path} with ${dataMap}")
        }
    }

    fun listenForData() {
        //don't forget to dispose it later
        rxWearBridge.dataSubject.subscribe {
            Log.d(TAG, "CapabilityInfo = ${it}")
        }
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
```

### Background usage (WearableListenerService)
Pretty much similar as the foreground usage except you need to extend the `RxWearBridgeListenerService` class.

After that you'll have access to `rxWearBridge` property that'll allow you to listen for (or send/sync) new messages/data/capability

```
class MyWearableListenerService : RxWearBridgeListenerService() {
    fun listenForMessage() {
        //don't forget to dispose it later
        rxWearBridge.messageSubject.subscribe {
            Log.d(TAG, "${it.path} from ${it.sourceNodeId}")
        }
    }

    fun listenForCapability() {
        //don't forget to dispose it later
        rxWearBridge.capabilitySubject.subscribe { (path, dataMap) ->
            Log.d(TAG, "${path} with ${dataMap}")
        }
    }

    fun listenForData() {
        //don't forget to dispose it later
        rxWearBridge.dataSubject.subscribe {
            Log.d(TAG, "CapabilityInfo = ${it}")
        }
    }
}

```

Don't forget to specify your service on you manifest with correct filter info to recieve data from wear device

## API
`RxWearBridge::messageSubject -> PublishSuject<MessageEvent>`
will give you all recieved messages

`RxWearBridge::dataSubject -> PublishSuject<Pair<String, DataMap>>`
will give you all recieved data, as path and data

`RxWearBridge::capabilitySubject -> PublishSuject<CapabilityInfo>`
will give you all recieved capability

`RxWearBridge::bindCapability(vararg capabilities: String)`
allow you to listen for capabilities by String, response will arrived on `capabilitySubject`

`RxWearBridge::unbindCapability(vararg capabilities: String)`
allow you to remove the listener for capabilities by String

`RxWearBridge::bindCapability(vararg data: Pair<Uri, Int>)`
allow you to listen for capabilities by Uri and filter, response will arrived on `capabilitySubject`

`RxWearBridge::bind()`
allow you to bind data and message reception

`RxWearBridge::unbind()`
allow you to unbind data and message reception

`RxWearBridge::sendMessage(path: String, dataMap: DataMap = DataMap(), capability: String? = null): Completable`
allow you to send a message with data to connected device, with the possibility to filter by capability

`RxWearBridge::syncData(path: String, dataMap: DataMap): Single<DataItem>`
allow you to sync data with connected devices, and return you the updated data

`RxWearBridge::getData(path: String, locally: Boolean = false): Maybe<DataMap>`
allow you to retrieve data from connected device

`RxWearBridge::syncDataArray(path: String, dataMaps: ArrayList<DataMap>): Single<DataItem>`
allow you to sync an array of data with connected devices, and return you the updated data

`RxWearBridge::getDataArray(path: String, locally: Boolean = false): Single<List<DataMap>>`
allow you to retrieve an array of data from connected devices or locally

`RxWearBridge::getAllData(path: String? = null): Single<List<DataMap>>`
allow you to retrieve all data from connected devices or specific one by putting his path

`RxWearBridge::syncBitmap(path: String, assetName: String, bitmap: Bitmap): Single<DataItem>`
allow you to sync a bitmap with connected devices, and return you the updated data

`RxWearBridge::getBitmap(path: String, assetName: String): Maybe<Bitmap>`
allow you to retrieve a bitmap from connected device

## Examples
This library come with a small example app, feel free to check it to see all the above cases.

## Deployment
### To bintray
```
./gradlew clean bintrayUpload
```

### Locally
```
./gradlew clean deployDebugLocally
```

## Licence

[MIT](https://github.com/jaumard/RxWearBridge/blob/master/LICENSE)
