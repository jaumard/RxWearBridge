package com.jaumard.wearbridge

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.util.Log
import com.jaumard.common.Contants
import com.jaumard.common.Contants.Companion.BITMAP_KEY
import com.jaumard.common.Contants.Companion.DATA_INT_KEY
import com.jaumard.common.Contants.Companion.DATA_KEY
import com.jaumard.rxwearbridge.RxWearBridge
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : WearableActivity() {
    companion object {
        private const val TAG = "WearableActivity"
    }

    private val rxWearBridge = RxWearBridge(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Enables Always-on
        setAmbientEnabled()
        rxWearBridge.messageSubject.subscribe {
            message.text = "${message.text}\n${it.path} from ${it.sourceNodeId}"
        }
        rxWearBridge.dataSubject.subscribe {
            message.text = "${message.text}\n${it.second.getString(DATA_KEY)} ${it.second.getInt(DATA_INT_KEY)}"
        }
        message.setOnClickListener {
            rxWearBridge.sendMessage(Contants.MESSAGE_PATH)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(onError = {
                        Log.e(TAG, "sendMessage", it)
                    }, onComplete = {
                        Log.i(TAG, "sendMessage ok")
                    })
        }

        getData()
        getDataArray()
        getAllData()
        getBitmap()
    }

    private fun getAllData() {
        rxWearBridge.getAllData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onError = {
                    Log.e(TAG, "getAllData", it)
                }, onSuccess = {
                    Log.i(TAG, "getAllData ok $it")
                })
    }

    private fun getData() {
        rxWearBridge.getData(Contants.DATA_PATH)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onError = {
                    Log.e(TAG, "getData", it)
                }, onSuccess = {
                    Log.i(TAG, "getData ok")
                    message.text = "${message.text}\n${it.getString(DATA_KEY)} ${it.getInt(DATA_INT_KEY)}"
                })
    }

    private fun getDataArray() {
        rxWearBridge.getDataArray(Contants.DATA_ARRAY_PATH)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onError = {
                    Log.e(TAG, "getDataArray", it)
                }, onSuccess = {
                    Log.i(TAG, "getDataArray ok")
                    it.forEach {
                        message.text = "${message.text}\n${it.getString(DATA_KEY)} ${it.getInt(DATA_INT_KEY)}"
                    }
                })
    }

    private fun getBitmap() {
        rxWearBridge.getBitmap(Contants.BITMAP_PATH, BITMAP_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onError = {
                    Log.e(TAG, "getBitmap", it)
                }, onSuccess = {
                    Log.i(TAG, "getBitmap ok")
                    container.background = BitmapDrawable(resources, it)
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
