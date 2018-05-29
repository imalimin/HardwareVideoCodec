/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.helper

import android.content.res.AssetManager
import java.io.BufferedReader
import java.io.InputStreamReader


/**
 * Created by lmyooyo@gmail.com on 2018/5/29.
 */
object AssetsHelper {
    fun read(assets: AssetManager, path: String): String {
        val reader = BufferedReader(InputStreamReader(assets.open(path),"UTF-8"))
        val buffer = StringBuffer()
        var str: String?
        str = reader.readLine()
        while (str != null) {
            buffer.append(str)
            buffer.append("\n")
            str = reader.readLine()
        }
        return buffer.toString()
    }
}