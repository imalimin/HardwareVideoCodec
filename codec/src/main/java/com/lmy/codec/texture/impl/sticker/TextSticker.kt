/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.lmy.codec.texture.impl.sticker

import android.graphics.*
import android.text.TextUtils

class TextSticker(width: Int = 0,
                  height: Int = 0,
                  textureId: IntArray = IntArray(1)) : BaseSticker(width, height, textureId) {
    private var textInfo: Text = Text("HWVC", 56f).apply {
        x = 0.8f
        y = 0.03f
    }
    private var bitmap: Bitmap? = null
    private val bitmapLock = Any()

    init {
        name = "TextSticker"
    }

    fun setText(text: Text) {
        this.textInfo = textInfo
        updateTexture()
    }

    fun getText(): String {
        return textInfo.text
    }

    private fun updateTexture() {
        val tmp = textInfo.create()
        synchronized(bitmapLock) {
            bitmap = tmp
        }
        updateSize(width, height)
    }

    override fun getRect(): RectF = textInfo.getRect(width, height)

    override fun init() {
        super.init()
        updateTexture()
    }

    override fun draw(transformMatrix: FloatArray?) {
        synchronized(bitmapLock) {
            if (null != bitmap && !bitmap!!.isRecycled) {
                bindTexture(bitmap!!)
            }
        }
        active()
        draw()
        inactive()
    }

    class Text(var text: String,
               var textSize: Float,
               var color: Int = Color.WHITE,
               var backgroundColor: Int = Color.TRANSPARENT,
               var typeface: Typeface = Typeface.DEFAULT_BOLD) : BaseSticker.Sticker() {
        private var paint: Paint = Paint()

        private fun updatePaint() {
            paint.isAntiAlias = true
            paint.textSize = textSize
            paint.color = color
            paint.textAlign = Paint.Align.LEFT
            paint.typeface = typeface
        }

        fun create(): Bitmap {
            updatePaint()
            val size = measureTextSize(paint, text)
            this.size.width = Math.ceil(size[0].toDouble()).toInt()
            this.size.height = Math.ceil(size[1].toDouble()).toInt()
            val bitmap = Bitmap.createBitmap(this.size.width, this.size.height,
                    Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawFilter = PaintFlagsDrawFilter(0,
                    Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            canvas.drawColor(backgroundColor)
            canvas.drawText(text, 0f, size[1], paint)
            return bitmap
        }

        /**
         * 测量文本宽高
         *
         * @param paint 画笔
         * @param text  文本
         * @return 宽高
         */
        private fun measureTextSize(paint: Paint, text: String): FloatArray {
            if (TextUtils.isEmpty(text)) return floatArrayOf(0f, 0f)
            val width = paint.measureText(text, 0, text.length)
            val bounds = Rect()
            paint.getTextBounds(text, 0, text.length, bounds)
            return floatArrayOf(width, bounds.height().toFloat())
        }
    }
}