package com.lmy.codec.entity

import android.graphics.SurfaceTexture
import android.opengl.*
import android.view.Surface
import com.lmy.codec.util.debug_e
import javax.microedition.khronos.egl.EGL10


/**
 * Created by lmyooyo@gmail.com on 2018/3/26.
 */
class Egl(var eglDisplay: EGLDisplay? = null,
          var eglConfig: EGLConfig? = null,
          var eglSurface: EGLSurface? = null,
          var eglContext: EGLContext? = null) {
    companion object {
        private val EGL_RECORDABLE_ANDROID = 0x3142
    }

//    fun initEGL(surfaceTexture: SurfaceTexture) {
//        //获取系统的EGL对象
//        mEgl = EGLContext.getEGL() as EGL10
//
//        //获取显示设备
//        eglDisplay = mEgl!!.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
//        if (eglDisplay === EGL10.EGL_NO_DISPLAY) {
//            throw RuntimeException("eglGetDisplay failed! " + mEgl!!.eglGetError())
//        }
//
//        //version中存放当前的EGL版本号，版本号即为version[0].version[1]，如1.0
//        val version = IntArray(2)
//
//        //初始化EGL
//        if (!mEgl!!.eglInitialize(eglDisplay, version)) {
//            throw RuntimeException("eglInitialize failed! " + mEgl!!.eglGetError())
//        }
//
//        //构造需要的配置列表
//        val attributes = intArrayOf(
//                //颜色缓冲区所有颜色分量的位数
//                EGL10.EGL_BUFFER_SIZE, 32,
//                //颜色缓冲区R、G、B、A分量的位数
//                EGL10.EGL_RED_SIZE, 8, EGL10.EGL_GREEN_SIZE, 8, EGL10.EGL_BLUE_SIZE, 8, EGL10.EGL_ALPHA_SIZE, 8, EGL10.EGL_NONE)
//        val configsNum = IntArray(1)
//
//        val configs = arrayOfNulls<EGLConfig>(1)
//        //EGL根据attributes选择最匹配的配置
//        if (!mEgl!!.eglChooseConfig(eglDisplay, attributes, configs, 1, configsNum)) {
//            throw RuntimeException("eglChooseConfig failed! " + mEgl!!.eglGetError())
//        }
//        eglConfig = configs[0]
//
//        //根据SurfaceTexture创建EGL绘图表面
//        eglSurface = mEgl!!.eglCreateWindowSurface(eglDisplay, eglConfig, surfaceTexture, null)
//
//        //指定哪个版本的OpenGL ES上下文，本文为OpenGL ES 2.0
//        val contextAttribs = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE)
//        //创建上下文，EGL10.EGL_NO_CONTEXT表示不和别的上下文共享资源
//        eglContext = mEgl!!.eglCreateContext(eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, contextAttribs)
//
//        if (eglDisplay === EGL10.EGL_NO_DISPLAY || eglContext === EGL10.EGL_NO_CONTEXT) {
//            throw RuntimeException("eglCreateContext fail failed! " + mEgl!!.eglGetError())
//        }
//    }

    fun initEGL() {
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (EGL14.EGL_NO_DISPLAY === eglDisplay) {
            debug_e("eglGetDisplay,failed:" + GLUtils.getEGLErrorString(EGL14.eglGetError()))
            return
        }
        val versions = IntArray(2)
        if (!EGL14.eglInitialize(eglDisplay, versions, 0, versions, 1)) {
            debug_e("eglInitialize,failed:" + GLUtils.getEGLErrorString(EGL14.eglGetError()))
            return
        }
        val configsCount = IntArray(1)
        val configs = arrayOfNulls<EGLConfig>(1)
        val configSpec = intArrayOf(EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT, EGL14.EGL_RED_SIZE, 8, EGL14.EGL_GREEN_SIZE, 8, EGL14.EGL_BLUE_SIZE, 8, EGL14.EGL_DEPTH_SIZE, 0, EGL14.EGL_STENCIL_SIZE, 0, EGL14.EGL_NONE)
        EGL14.eglChooseConfig(eglDisplay, configSpec, 0, configs, 0, 1, configsCount, 0)
        if (configsCount[0] <= 0) {
            debug_e("eglChooseConfig,failed:" + GLUtils.getEGLErrorString(EGL14.eglGetError()))
            return
        }
        eglConfig = configs[0]
        val surfaceAttribs = intArrayOf(EGL10.EGL_WIDTH, 1, EGL10.EGL_HEIGHT, 1, EGL14.EGL_NONE)
        val contextSpec = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
        eglContext = EGL14.eglCreateContext(eglDisplay, eglConfig, EGL14.EGL_NO_CONTEXT, contextSpec, 0)
        if (EGL14.EGL_NO_CONTEXT === eglContext) {
            debug_e("eglCreateContext,failed:" + GLUtils.getEGLErrorString(EGL14.eglGetError()))
            return
        }
        val values = IntArray(1)
        EGL14.eglQueryContext(eglDisplay, eglContext, EGL14.EGL_CONTEXT_CLIENT_VERSION, values, 0)
        eglSurface = EGL14.eglCreatePbufferSurface(eglDisplay, eglConfig, surfaceAttribs, 0)
        if (null == eglSurface || EGL14.EGL_NO_SURFACE == eglSurface) {
            debug_e("eglCreateWindowSurface,failed:" + GLUtils.getEGLErrorString(EGL14.eglGetError()))
        }
    }

    fun initEGL(surfaceTexture: SurfaceTexture, context: EGLContext?) {
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (EGL14.EGL_NO_DISPLAY === eglDisplay) {
            debug_e("eglGetDisplay,failed:" + GLUtils.getEGLErrorString(EGL14.eglGetError()))
            return
        }
        val versions = IntArray(2)
        if (!EGL14.eglInitialize(eglDisplay, versions, 0, versions, 1)) {
            debug_e("eglInitialize,failed:" + GLUtils.getEGLErrorString(EGL14.eglGetError()))
            return
        }
        val configsCount = IntArray(1)
        val configs = arrayOfNulls<EGLConfig>(1)
        val configSpec = intArrayOf(EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT, EGL14.EGL_RED_SIZE, 8, EGL14.EGL_GREEN_SIZE, 8, EGL14.EGL_BLUE_SIZE, 8, EGL14.EGL_DEPTH_SIZE, 0, EGL14.EGL_STENCIL_SIZE, 0, EGL14.EGL_NONE)
        EGL14.eglChooseConfig(eglDisplay, configSpec, 0, configs, 0, 1, configsCount, 0)
        if (configsCount[0] <= 0) {
            debug_e("eglChooseConfig,failed:" + GLUtils.getEGLErrorString(EGL14.eglGetError()))
            return
        }
        eglConfig = configs[0]
        val surfaceAttribs = intArrayOf(EGL14.EGL_NONE)
        val contextSpec = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
        eglContext = EGL14.eglCreateContext(eglDisplay, eglConfig,
                context ?: EGL14.EGL_NO_CONTEXT, contextSpec, 0)
        if (EGL14.EGL_NO_CONTEXT === eglContext) {
            debug_e("eglCreateContext,failed:" + GLUtils.getEGLErrorString(EGL14.eglGetError()))
            return
        }
        val values = IntArray(1)
        EGL14.eglQueryContext(eglDisplay, eglContext, EGL14.EGL_CONTEXT_CLIENT_VERSION, values, 0)
        eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, eglConfig, surfaceTexture, surfaceAttribs, 0)
        if (null == eglSurface || EGL14.EGL_NO_SURFACE == eglSurface) {
            debug_e("eglCreateWindowSurface,failed:" + GLUtils.getEGLErrorString(EGL14.eglGetError()))
            return
        }
    }

    fun initEGL(surface: Surface, context: EGLContext?) {
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (EGL14.EGL_NO_DISPLAY === eglDisplay) {
            //            throw new RuntimeException("eglGetDisplay,failed:" + GLUtils.getEGLErrorString(EGL14.eglGetError()));
            debug_e("eglGetDisplay,failed:" + GLUtils.getEGLErrorString(EGL14.eglGetError()))
            return
        }
        val versions = IntArray(2)
        if (!EGL14.eglInitialize(eglDisplay, versions, 0, versions, 1)) {
            debug_e("eglInitialize,failed:" + GLUtils.getEGLErrorString(EGL14.eglGetError()))
            return
        }
        val configsCount = IntArray(1)
        val configs = arrayOfNulls<EGLConfig>(1)
        val configSpec = intArrayOf(EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT, EGL14.EGL_RED_SIZE,
                8, EGL14.EGL_GREEN_SIZE, 8, EGL14.EGL_BLUE_SIZE, 8, EGL_RECORDABLE_ANDROID,
                1, EGL14.EGL_DEPTH_SIZE, 0, EGL14.EGL_STENCIL_SIZE, 0, EGL14.EGL_NONE)
        EGL14.eglChooseConfig(eglDisplay, configSpec, 0, configs, 0, 1, configsCount, 0)
        if (configsCount[0] <= 0) {
            debug_e("eglChooseConfig,failed:" + GLUtils.getEGLErrorString(EGL14.eglGetError()))
            return
        }
        eglConfig = configs[0]
        val surfaceAttribs = intArrayOf(EGL14.EGL_NONE)
        val contextSpec = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
        eglContext = EGL14.eglCreateContext(eglDisplay, eglConfig,
                context ?: EGL14.EGL_NO_CONTEXT, contextSpec, 0)
        if (EGL14.EGL_NO_CONTEXT === eglContext) {
            debug_e("eglCreateContext,failed:" + GLUtils.getEGLErrorString(EGL14.eglGetError()))
            return
        }
        val values = IntArray(1)
        EGL14.eglQueryContext(eglDisplay, eglContext, EGL14.EGL_CONTEXT_CLIENT_VERSION, values, 0)
        eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, eglConfig, surface, surfaceAttribs, 0)
        if (null == eglSurface || EGL14.EGL_NO_SURFACE == eglSurface) {
            debug_e("eglCreateWindowSurface,failed:" + GLUtils.getEGLErrorString(EGL14.eglGetError()))
        }
    }

    fun makeCurrent() {
        makeCurrent(null)
    }
    fun makeCurrent(tag:String?) {
        if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
            //            throw new RuntimeException("eglMakeCurrent,failed:" + GLUtils.getEGLErrorString(EGL14.eglGetError()));
            debug_e("eglMakeCurrent,failed:" + GLUtils.getEGLErrorString(EGL14.eglGetError())+", $tag")
        }
    }

    fun swapBuffers() {
        if (!EGL14.eglSwapBuffers(eglDisplay, eglSurface)) {
            debug_e("eglSwapBuffers,failed!")
        }
    }

    fun release() {
        makeCurrent()
        EGL14.eglDestroySurface(eglDisplay, eglSurface)
        EGL14.eglDestroyContext(eglDisplay, eglContext)
        EGL14.eglTerminate(eglDisplay)
        EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
    }
}