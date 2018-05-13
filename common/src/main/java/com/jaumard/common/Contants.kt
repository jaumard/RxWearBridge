package com.jaumard.common

import android.util.Log

const val MESSAGE_PATH = "/message"
const val DATA_PATH = "/data"
const val DATA_ARRAY_PATH = "/dataarray"
const val BITMAP_PATH = "/bitmap"
const val BITMAP_KEY = "image"
const val DATA_KEY = "data"
const val DATA_INT_KEY = "dataInt"

fun Any.debug(message: Any) {
    if (BuildConfig.DEBUG) Log.d(this::class.java.simpleName, message.toString())
}

fun Any.error(message: String? = null, throwable: Throwable) {
    Log.e(this::class.java.simpleName, message, throwable)
}