package com.jaumard.wearbridge

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.android.gms.wearable.DataMap
import com.jaumard.common.*
import com.jaumard.rxwearbridge.RxWearBridge
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val rxWearBridge = RxWearBridge(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rxWearBridge.messageSubject.subscribe {
            message.text = "${message.text}\n${it.path} from ${it.sourceNodeId}"
            debug("${it.path} from ${it.sourceNodeId}")
        }

        rxWearBridge.dataSubject.subscribe { (path, data) ->

        }

        syncData()
        syncDataArray()
        syncBitmap()
        getUnknownData()
        getUnknownBitmap()
        getUnknownDataArray()
        getAllDataUnknown()
    }

    private fun getUnknownBitmap() {
        rxWearBridge.getBitmap("/unknown/path", "test")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onError = {
                    error("unknown bitmap", it)
                }, onSuccess = {
                    debug(it)
                    assert(false)//must not return a value
                })
    }

    private fun getAllDataUnknown() {
        rxWearBridge.getAllData("/unknown/path")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onError = {
                    error("unknown all data", it)
                }, onSuccess = {
                    debug(it)
                    assert(it.isEmpty())
                })
    }

    private fun getUnknownDataArray() {
        rxWearBridge.getDataArray("/unknown/path", true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onError = {
                    error("unknown data array local", it)
                }, onSuccess = {
                    debug(it)
                    assert(it.isEmpty())
                })

        rxWearBridge.getDataArray("/unknown/path")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onError = {
                    error("unknown data array", it)
                }, onSuccess = {
                    debug(it)
                    assert(it.isEmpty())
                })
    }

    private fun getUnknownData() {
        rxWearBridge.getData("/unknown/path", true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onError = {
                    error("unknown data local", it)
                }, onSuccess = {
                    debug(it)
                    assert(false)//must not return a value
                })

        rxWearBridge.getData("/unknown/path")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onError = {
                    error("unknown data", it)
                }, onSuccess = {
                    debug(it)
                    assert(false)//must not return a value
                })
    }

    private fun syncBitmap() {
        val bitmapIcon = BitmapFactory.decodeResource(resources, R.drawable.profil)
        rxWearBridge.syncBitmap(BITMAP_PATH, BITMAP_KEY, bitmapIcon)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onError = {
                    error("syncBitmap", it)
                }, onSuccess = {
                    debug("syncBitmap ok $it")
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
        rxWearBridge.syncDataArray(DATA_ARRAY_PATH, dataMapArray)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onError = {
                    error("syncDataArray", it)
                }, onSuccess = {
                    debug("syncDataArray ok $it")
                })
    }

    private fun syncData() {
        val dataMap = DataMap()
        dataMap.putString(DATA_KEY, "myDataSync")
        dataMap.putInt(DATA_INT_KEY, 9)
        rxWearBridge.syncData(DATA_PATH, dataMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onError = {
                    error("syncData", it)
                }, onSuccess = {
                    debug("syncData ok $it")
                })
    }

    fun updateData(v: View) {
        val dataMap = DataMap()
        dataMap.putString(DATA_KEY, "myDataSyncUpdated")
        dataMap.putInt(DATA_INT_KEY, Math.round(Math.random() * 100).toInt())
        rxWearBridge.syncData(DATA_PATH, dataMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onError = {
                    error("updateData", it)
                }, onSuccess = {
                    debug("updateData ok $it")
                })
    }

    fun sendMessage(v: View) {
        rxWearBridge.sendMessage(MESSAGE_PATH)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onError = {
                    error("sendMessage", it)
                }, onComplete = {
                    debug("sendMessage ok")
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
