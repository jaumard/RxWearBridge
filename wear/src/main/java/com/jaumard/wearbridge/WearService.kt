package com.jaumard.wearbridge

import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class WearService : WearableListenerService() {
    companion object {
        private const val TAG = "WearService"
    }

    override fun onMessageReceived(message: MessageEvent) {
        super.onMessageReceived(message)
        Log.d(TAG, "${message.path} from ${message.sourceNodeId}")
        startActivity(Intent(this, MainActivity::class.java))
    }
}