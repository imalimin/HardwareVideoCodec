/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.util

import android.util.Log
import java.util.*

/**
 * Created by lmyooyo@gmail.com on 2018/3/23.
 */
inline fun <reified T> T.debug_v(vararg objects: Any?) {
    if (!Debuger.Debug) return
    if (objects.isEmpty()) {
        Log.v(T::class.simpleName, "null")
        return
    }
    var msg = if (objects.size == 1) objects[0].toString() else Arrays.toString(objects)
    Log.v(T::class.simpleName, msg)
}

inline fun <reified T> T.debug_d(vararg objects: Any?) {
    if (!Debuger.Debug) return
    if (objects.isEmpty()) {
        Log.d(T::class.simpleName, "null")
        return
    }
    var msg = if (objects.size == 1) objects[0].toString() else Arrays.toString(objects)
    Log.d(T::class.simpleName, msg)
}

inline fun <reified T> T.debug_w(vararg objects: Any?) {
    if (!Debuger.Debug) return
    if (objects.isEmpty()) {
        Log.w(T::class.simpleName, "null")
        return
    }
    var msg = if (objects.size == 1) objects[0].toString() else Arrays.toString(objects)
    Log.w(T::class.simpleName, msg)
}

inline fun <reified T> T.debug_i(vararg objects: Any?) {
    if (!Debuger.Debug) return
    if (objects.isEmpty()) {
        Log.i(T::class.simpleName, "null")
        return
    }
    var msg = if (objects.size == 1) objects[0].toString() else Arrays.toString(objects)
    Log.i(T::class.simpleName, msg)
}

inline fun <reified T> T.debug_e(vararg objects: Any?) {
    if (!Debuger.Debug) return
    if (objects.isEmpty()) {
        Log.e(T::class.simpleName, "null")
        return
    }
    var msg = if (objects.size == 1) objects[0].toString() else Arrays.toString(objects)
    Log.e(T::class.simpleName, msg)
}

class Debuger {
    companion object {
        var Debug = true
    }
}