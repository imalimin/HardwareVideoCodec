package com.lmy.codec.texture.impl.sticker

import android.graphics.*
import android.text.TextUtils

class TextSticker(frameBuffer: IntArray,
                  width: Int,
                  height: Int,
                  name: String = "BaseSticker") : BaseSticker(frameBuffer, width, height, name) {
    private var textInfo: Text = Text("HWVC", 46f, Color.WHITE)
    private var bitmap: Bitmap? = null
    private val bitmapLock = Any()

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
        val rect = textInfo.getRect(width, height)
        updateLocation(floatArrayOf(
                0f, 0f,//LEFT,BOTTOM
                1f, 0f,//RIGHT,BOTTOM
                0f, 1f,//LEFT,TOP
                1f, 1f//RIGHT,TOP
        ), floatArrayOf(
                rect.left, rect.bottom,//LEFT,BOTTOM
                rect.right, rect.bottom,//RIGHT,BOTTOM
                rect.left, rect.top,//LEFT,TOP
                rect.right, rect.top//RIGHT,TOP
        ))
    }

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
               var size: Float,
               var color: Int,
               var backgroundColor: Int = Color.TRANSPARENT) : BaseSticker.Sticker() {
        private var paint: Paint = Paint()
        private var textSize: FloatArray = FloatArray(2)

        private fun updatePaint() {
            paint.isAntiAlias = true
            paint.textSize = size
            paint.color = color
            paint.textAlign = Paint.Align.LEFT
        }

        fun create(): Bitmap {
            updatePaint()
            val size = measureTextSize(paint, text)
            textSize[0] = size[0]
            textSize[1] = size[1]
            val bitmap = Bitmap.createBitmap(Math.ceil(size[0].toDouble()).toInt(),
                    Math.ceil(size[1].toDouble()).toInt(),
                    Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawFilter = PaintFlagsDrawFilter(0,
                    Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            canvas.drawColor(backgroundColor)
            canvas.drawText(text, 0f, size[1], paint)
            return bitmap
        }

        fun getRect(width: Int, height: Int): RectF {
            val rect = RectF()
            rect.left = -1f
            rect.bottom = 1f
            rect.right = rect.left + textSize[0] / width.toFloat()
            rect.top = rect.bottom - textSize[1] / height.toFloat()
            rect.offset(x, y)
            return rect
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