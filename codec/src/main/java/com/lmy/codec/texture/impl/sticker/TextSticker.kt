package com.lmy.codec.texture.impl.sticker

import android.graphics.*
import android.text.TextUtils
import com.lmy.codec.util.debug_e

class TextSticker(frameBuffer: IntArray,
                  width: Int,
                  height: Int,
                  name: String = "BaseSticker") : BaseSticker(frameBuffer, width, height, name) {
    private var text: String = "HWVC"
    private var paint: Paint = Paint()
    private var bitmap: Bitmap? = null

    init {
        paint.strokeWidth = 46f
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.LEFT
    }

    fun setText(text: String, size: Float) {
        this.text = text
        paint.strokeWidth = size
        bitmap = createBitmap()
    }

    fun getText(): String {
        return text
    }

    private fun createBitmap(): Bitmap {
        val size = measureTextSize(paint, text)
        val bitmap = Bitmap.createBitmap(Math.ceil(size[0].toDouble()).toInt(),
                Math.ceil(size[1].toDouble()).toInt(),
                Bitmap.Config.ARGB_8888)
        debug_e("createBitmap " + bitmap.width + "x" + bitmap.height)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.RED)
        canvas.drawText(getText(), 0f, size[1], paint)
//        val out = FileOutputStream("${Environment.getExternalStorageDirectory().absolutePath}/ttttt.jpg")
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
//        out.close()
        return bitmap
    }

    override fun init() {
        super.init()
        bitmap = createBitmap()
    }

    override fun draw(transformMatrix: FloatArray?) {
        if (null != bitmap && !bitmap!!.isRecycled) {
            debug_e("bindTexture")
            bindTexture(bitmap!!)
        }
        active()
        draw()
        inactive()
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