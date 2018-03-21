package com.lmy.codec

import android.util.Log
import java.util.*

/**
 * Created by lmyooyo@gmail.com on 2018/3/21.
 */
inline fun <reified T> T.logv(vararg objects: Any?) {
    if (objects.isEmpty()) {
        Log.v(T::class.simpleName, "null")
        return
    }
    var msg = if (objects.size == 1) objects[0].toString() else Arrays.toString(objects)
    Log.v(T::class.simpleName, msg)
}

inline fun <reified T> T.logd(vararg objects: Any?) {
    if (objects.isEmpty()) {
        Log.d(T::class.simpleName, "null")
        return
    }
    var msg = if (objects.size == 1) objects[0].toString() else Arrays.toString(objects)
    Log.d(T::class.simpleName, msg)
}

inline fun <reified T> T.logw(vararg objects: Any?) {
    if (objects.isEmpty()) {
        Log.w(T::class.simpleName, "null")
        return
    }
    var msg = if (objects.size == 1) objects[0].toString() else Arrays.toString(objects)
    Log.w(T::class.simpleName, msg)
}

inline fun <reified T> T.logi(vararg objects: Any?) {
    if (objects.isEmpty()) {
        Log.i(T::class.simpleName, "null")
        return
    }
    var msg = if (objects.size == 1) objects[0].toString() else Arrays.toString(objects)
    Log.i(T::class.simpleName, msg)
}

inline fun <reified T> T.loge(vararg objects: Any?) {
    if (objects.isEmpty()) {
        Log.e(T::class.simpleName, "null")
        return
    }
    var msg = if (objects.size == 1) objects[0].toString() else Arrays.toString(objects)
    Log.e(T::class.simpleName, msg)
}