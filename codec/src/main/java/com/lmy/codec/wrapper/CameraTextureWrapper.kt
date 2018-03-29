package com.lmy.codec.wrapper

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import com.lmy.codec.entity.Egl
import com.lmy.codec.texture.impl.BaseFrameBufferTexture
import com.lmy.codec.texture.impl.CameraTexture
import com.lmy.codec.util.debug_e
import javax.microedition.khronos.opengles.GL10


/**
 * Created by lmyooyo@gmail.com on 2018/3/26.
 */
class CameraTextureWrapper(var texture: BaseFrameBufferTexture? = null) : TextureWrapper() {

    init {
        textureId = createTexture()
        intTexture()
    }

    fun initEGL() {
        egl = Egl()
        egl!!.initEGL()
        egl!!.makeCurrent()
        texture = CameraTexture(textureId!!)
        debug_e("camera textureId: ${textureId}")
    }

    private fun createTexture(): Int {
        val texture = IntArray(1)
        GLES20.glGenTextures(1, texture, 0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0])
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST.toFloat())
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE.toFloat())
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
        return texture[0]
    }

    @SuppressLint("Recycle")
    private fun intTexture() {
        if (null != textureId)
            surfaceTexture = SurfaceTexture(textureId!!)
    }

    override fun drawTexture(transformMatrix: FloatArray?) {
        if (null == texture) {
            debug_e("Render failed. Texture is null")
            return
        }
        texture?.drawTexture(transformMatrix)
    }
}