package com.lmy.codec.texture.impl.sticker

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextUtils

class TextSticker(frameBuffer: IntArray,
                  width: Int,
                  height: Int,
                  name: String = "BaseSticker") : BaseSticker(frameBuffer, width, height, name) {
    private var text: String = "HWVC"
    private var paint: Paint = Paint()
    private var bitmap: Bitmap? = null

    init {
        paint.strokeWidth = 23f
        paint.color = Color.WHITE
    }

    fun setText(text: String, size: Float) {
        this.text = text
        paint.strokeWidth = size
    }

    private fun createBitmap(): Bitmap {
        val size = measureTextSize(paint, text)
        val bitmap = Bitmap.createBitmap(Math.ceil(size[0].toDouble()).toInt(),
                Math.ceil(size[1].toDouble()).toInt(),
                Bitmap.Config.ARGB_8888)
        return bitmap
    }

    override fun init() {
        super.init()
    }

    override fun draw(transformMatrix: FloatArray?) {
        if (null != bitmap && !bitmap!!.isRecycled) {

        }
    }

    /**
     * 测量文本宽高
     *
     * @param paint 画笔
     * @param text  文本
     * @return 宽高
     */
    fun measureTextSize(paint: Paint, text: String): FloatArray {
        if (TextUtils.isEmpty(text)) return floatArrayOf(0f, 0f)
        val width = paint.measureText(text, 0, text.length)
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        return floatArrayOf(width, bounds.height().toFloat())
    }
}