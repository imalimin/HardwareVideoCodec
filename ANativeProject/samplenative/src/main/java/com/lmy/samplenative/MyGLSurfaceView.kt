package com.lmy.samplenative

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.os.Environment
import android.util.AttributeSet
import android.view.Surface
import com.lmy.hwvcnative.processor.PictureProcessor
import kotlinx.android.synthetic.main.activity_main.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLSurfaceView : GLSurfaceView {

    constructor(context: Context) : super(context) {
        init();
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init();
    }

    private fun init() {
        setEGLConfigChooser(8, 8, 8, 0, 16, 0)
        setEGLContextClientVersion(3)
        setRenderer(Renderer())
    }

    class Renderer() : GLSurfaceView.Renderer {
        private var processor: PictureProcessor? = PictureProcessor()
        override fun onDrawFrame(gl: GL10?) {
            processor?.invalidate()
        }

        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            processor?.prepare(Surface(SurfaceTexture(0)), width, height)
            processor?.show("${Environment.getExternalStorageDirectory().path}/1.jpg")
        }

        override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        }
    }
}