package com.lmy.codec.helper

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Created by lmyooyo@gmail.com on 2018/8/9.
 */
class Resources private constructor() {
    private object Holder {
        @SuppressLint("StaticFieldLeak")
        val instance = Resources()
    }

    companion object {
        val instance: Resources by lazy { Holder.instance }
    }

    fun isSupportPBO(): Boolean {
        check()
        return GLHelper.isSupportPBO(ctx!!)
    }

    fun readAssetsAsString(path: String): String {
        check()
        val reader = BufferedReader(InputStreamReader(ctx!!.assets.open(path), "UTF-8"))
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

    fun readAssetsAsBitmap(path: String): Bitmap? {
        check()
        var inStream: InputStream? = null
        return try {
            inStream = ctx!!.assets.open(path)
            BitmapFactory.decodeStream(inStream)
        } catch (e: Exception) {
            null
        } finally {
            inStream?.close()
        }
    }

    private var ctx: Context? = null
    fun attach(ctx: Context) {
        this.ctx = ctx
    }

    fun dettach() {
        this.ctx = null
    }

    private fun check() {
        if (null == ctx)
            throw RuntimeException("Resources`s instance has dettach!")
    }
}