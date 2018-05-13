package com.jaumard.rxwearbridge

import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

open class RxWearBridgeListenerService : WearableListenerService() {
    protected val rxWearBridge by lazy { RxWearBridge(this) }

    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        super.onDataChanged(dataEventBuffer)
        rxWearBridge.onDataChanged(dataEventBuffer)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        rxWearBridge.onMessageReceived(messageEvent)
    }

    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        super.onCapabilityChanged(capabilityInfo)
        rxWearBridge.onCapabilityChanged(capabilityInfo)
    }
}