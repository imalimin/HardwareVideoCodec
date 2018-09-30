package com.lmy.codec.texture.impl.sticker

import android.graphics.Bitmap
import android.graphics.RectF

class ImageSticker(width: Int = 0,
                   height: Int = 0,
                   textureId: IntArray = IntArray(1)) : BaseSticker(width, height, textureId) {
    private var image: Image = Image()
    private val bitmapLock = Any()

    init {
        name = "ImageSticker"
    }

    override fun init() {
        super.init()
        updateSize(width, height)
    }

    fun setImage(image: Image) {
        synchronized(bitmapLock) {
            this.image = image
        }
        updateSize(width, height)
    }

    override fun getRect(): RectF = image.getRect(width, height)

    override fun draw(transformMatrix: FloatArray?) {
        synchronized(bitmapLock) {
            if (null != image.bitmap && !image.bitmap!!.isRecycled) {
                bindTexture(image.bitmap!!)
            }
        }
        active()
        draw()
        inactive()
    }

    class Image : BaseSticker.Sticker() {
        var bitmap: Bitmap? = null
            set(value) {
                field = value
                if (null == value) return
                this.size.width = value.width
                this.size.height = value.height
            }
    }
}