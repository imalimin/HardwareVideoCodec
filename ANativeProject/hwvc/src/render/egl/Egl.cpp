/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/Egl.h"
#include "log.h"
#include <android/native_window_jni.h>

//eglGetProcAddress( "eglPresentationTimeANDROID")
/**
 * 1. eglGetConfigs
 * 2. eglBindAPI
 * 3. eglSwapInterval
 * 4. ANativeWindow_setBuffersGeometry
 */
Egl::Egl() {
    init(nullptr, nullptr);
}

Egl::Egl(Egl *context) {
    init(context, nullptr);
}

Egl::Egl(HwWindow *win) {
    init(nullptr, win);
}

Egl::Egl(Egl *context, HwWindow *win) {
    init(context, win);
}

Egl::~Egl() {
    if (eglDisplay != EGL_NO_DISPLAY) {
        if (!eglMakeCurrent(eglDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT)) {
            checkError();
            LOGE("~Egl makeCurrent failed");
        }
        if (eglSurface == EGL_NO_SURFACE || EGL_TRUE != eglDestroySurface(eglDisplay, eglSurface)) {
            LOGE("~Egl eglDestroySurface failed");
        }
        if (eglContext == EGL_NO_CONTEXT || EGL_TRUE != eglDestroyContext(eglDisplay, eglContext)) {
            LOGE("~Egl eglDestroyContext failed");
        }
        if (EGL_TRUE != eglTerminate(eglDisplay)) {
            LOGE("~Egl eglTerminate failed");
        }
    }
    eglContext = EGL_NO_CONTEXT;
    eglSurface = EGL_NO_SURFACE;
    eglDisplay = EGL_NO_DISPLAY;
    eglConfig = nullptr;
    if (this->win) {
        delete this->win;
        this->win = nullptr;
    }
    Logcat::i("HWVC", "Egl::~Egl");
}

void Egl::init(Egl *context, HwWindow *win) {
    if (EGL_NO_DISPLAY != eglDisplay
        || EGL_NO_SURFACE != eglContext
        || EGL_NO_SURFACE != eglSurface) {
        Logcat::e("HWVC", "Dirty env!!!!!!!!!!");
        return;
    }
    this->win = win;
    createDisplay(EGL_DEFAULT_DISPLAY);
    if (EGL_NO_DISPLAY == this->eglDisplay) {
        LOGE("$s failed", __func__);
        return;
    }
    if (win && win->getANativeWindow()) {
        createConfig(CONFIG_WIN);
    } else {
        createConfig(CONFIG_BUFFER);
    }
    if (!this->eglConfig) {
        LOGE("$s bad config", __func__);
        return;
    }
    if (this->win) {
        createWindowSurface(this->win);
    } else {
        createPbufferSurface();
    }
    if (EGL_NO_SURFACE == this->eglSurface) {
        LOGE("$s bad surface", __func__);
        return;
    }
    if (context) {
        createContext(context->eglContext);
    } else {
        createContext(EGL_NO_CONTEXT);
    }
    if (EGL_NO_CONTEXT == this->eglContext) {
        LOGE("$s bad context", __func__);
        return;
    }
    makeCurrent();
    int width = 0, height = 0;
    if (!eglQuerySurface(eglDisplay, eglSurface, EGL_WIDTH, &width) ||
        !eglQuerySurface(eglDisplay, eglSurface, EGL_HEIGHT, &height)) {
        Logcat::e("HWVC", "Egl init failed");
    }
    //If interval is set to a value of 0, buffer swaps are not synchronized to a video frame, and the swap happens as soon as the render is complete.
//    eglSwapInterval(eglDisplay, 0);
}

EGLDisplay Egl::createDisplay(EGLNativeDisplayType display_id) {
    eglDisplay = eglGetDisplay(display_id);
    if (EGL_NO_DISPLAY == eglDisplay || !checkError()) {
        LOGE("eglGetDisplay failed");
        return EGL_NO_DISPLAY;
    }
    EGLint majorVersion;
    EGLint minorVersion;
    if (!eglInitialize(eglDisplay, // 创建的EGL连接
                       &majorVersion, // 返回EGL主板版本号
                       &minorVersion) || !checkError()) { // 返回EGL次版本号
        LOGE("eglInitialize failed");
        return EGL_NO_DISPLAY;
    }
    return eglDisplay;
}

EGLConfig Egl::createConfig(const int *configSpec) {
    EGLint configsCount = 0;
    const EGLint maxConfigs = 2;
    EGLConfig configs[maxConfigs];
    //Get a list of all EGL frame buffer configurations for a display
    EGLBoolean ret = eglGetConfigs(eglDisplay, configs, maxConfigs, &configsCount);
    if (ret != EGL_TRUE || configsCount <= 0) {
        LOGE("eglChooseConfig failed");
        return nullptr;
    }

    // Get a list of EGL frame buffer configurations that match specified attributes
    ret = eglChooseConfig(eglDisplay, // 创建的和本地窗口系统的连接
                          configSpec, // 指定渲染表面的参数列表，可以为null
                          configs, // 调用成功，返会符合条件的EGLConfig列表
                          maxConfigs, // 最多返回的符合条件的EGLConfig个数
                          &configsCount); // 实际返回的符合条件的EGLConfig个数
    if (EGL_TRUE != ret || configsCount <= 0 || !checkError()) {
        LOGE("eglChooseConfig failed");
        return nullptr;
    }
    eglConfig = configs[0];
    if (win && win->getANativeWindow()) {
        EGLint format;
        if (EGL_TRUE != eglGetConfigAttrib(eglDisplay, eglConfig, EGL_NATIVE_VISUAL_ID, &format)) {
            Logcat::e("HWVC", "eglGetConfigAttrib failed");
        } else {
            ANativeWindow_setBuffersGeometry(win->getANativeWindow(), 0, 0, format);
        }
    }
    return configs[0];
}

EGLContext Egl::createContext(EGLContext context) {
    int contextSpec[] = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE};
    eglContext = eglCreateContext(eglDisplay, eglConfig, context, contextSpec);
    if (EGL_NO_CONTEXT == eglContext || !checkError()) {
        LOGE("eglCreateContext failed");
        return EGL_NO_CONTEXT;
    }
    return eglContext;
}

EGLSurface Egl::createPbufferSurface() {
//    EGLint values;
//    eglQueryContext(eglDisplay, eglContext, EGL_CONTEXT_CLIENT_VERSION, &values);
    int attrib_list[] = {EGL_WIDTH, 1, EGL_HEIGHT, 1, EGL_NONE};
    eglSurface = eglCreatePbufferSurface(eglDisplay, eglConfig, attrib_list);
    if (nullptr == eglSurface || EGL_NO_SURFACE == eglSurface || !checkError()) {
        LOGE("eglCreatePbufferSurface failed");
        return EGL_NO_SURFACE;
    }
    EGLBoolean ret = eglBindAPI(EGL_OPENGL_ES_API);
    if (EGL_TRUE != ret) {
        LOGE("eglBindAPI failed");
        return EGL_NO_SURFACE;
    }
    return eglSurface;
}

EGLSurface Egl::createWindowSurface(HwWindow *win) {
//    EGLint values;
//    eglQueryContext(eglDisplay, eglContext, EGL_CONTEXT_CLIENT_VERSION, &values);
    int attrib_list[] = {EGL_NONE};
    eglSurface = eglCreateWindowSurface(eglDisplay,
                                        eglConfig, // 选好的可用EGLConfig
                                        win->getANativeWindow(), // 指定原生窗口
                                        attrib_list); // 指定窗口属性列表，可以为null，一般指定渲染所用的缓冲区使用但缓冲或者后台缓冲，默认为后者。
    if (nullptr == eglSurface || EGL_NO_SURFACE == eglSurface || !checkError()) {
        LOGE("eglCreateWindowSurface failed");
        return EGL_NO_SURFACE;
    }
    EGLBoolean ret = eglBindAPI(EGL_OPENGL_ES_API);
    if (EGL_TRUE != ret) {
        LOGE("eglBindAPI failed");
        return EGL_NO_SURFACE;
    }
    return eglSurface;
}

int Egl::width() {
    return getParams(EGL_WIDTH);
}

int Egl::height() {
    return getParams(EGL_HEIGHT);
}

EGLint Egl::getParams(EGLint attribute) {
    EGLint params;
    eglQuerySurface(eglDisplay, eglSurface, attribute, &params);
    return params;
}

void Egl::makeCurrent() {
    if (EGL_NO_DISPLAY == eglDisplay) {
        LOGE("name egl failed had release!");
        return;
    }
    if (!eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext) || !checkError()) {
        LOGE("name makeCurrent failed");
    }
}

void Egl::swapBuffers() {
    if (!eglSwapBuffers(eglDisplay, eglSurface)) {
        LOGE("name swapBuffers failed!");
    }
}

bool Egl::checkError() {
    EGLint error = eglGetError();
    if (EGL_SUCCESS != error) {
        LOGE("Bad EGL environment: %d", error);
        return false;
    }
    return true;
}
