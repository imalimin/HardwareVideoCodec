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
            LOGE("~Egl makeCurrent failed: %d", eglGetError());
        }
        if (eglContext != EGL_NO_CONTEXT){
            eglDestroyContext(eglDisplay, eglContext);
        }
        if (eglSurface != EGL_NO_SURFACE){
            eglDestroySurface(eglDisplay, eglSurface);
        }
        eglTerminate(eglDisplay);
    }
    eglContext = EGL_NO_CONTEXT;
    eglSurface = EGL_NO_SURFACE;
    eglDisplay = EGL_NO_DISPLAY;
    if (this->win) {
        delete this->win;
        this->win = nullptr;
    }
    LOGI("%s", __func__);
}

void Egl::init(Egl *context, HwWindow *win) {
    this->win = win;
    this->eglDisplay = createDisplay(EGL_DEFAULT_DISPLAY);
    if (!this->eglDisplay)
        return;
    this->eglConfig = createConfig(CONFIG_DEFAULT);
    if (!this->eglConfig)
        return;
    if (context) {
        this->eglContext = createContext(context->eglContext);
    } else {
        this->eglContext = createContext(EGL_NO_CONTEXT);
    }
    if (!this->eglContext)
        return;
    if (this->win) {
        this->eglSurface = createWindowSurface(this->win);
    } else {
        this->eglSurface = createPbufferSurface();
    }
}

EGLDisplay Egl::createDisplay(EGLNativeDisplayType display_id) {
    EGLDisplay eglDisplay = eglGetDisplay(display_id);
    if (EGL_NO_DISPLAY == eglDisplay || EGL_SUCCESS != eglGetError()) {
        LOGE("eglGetDisplay failed: %d", eglGetError());
        return EGL_NO_DISPLAY;
    }
    EGLint majorVersion;
    EGLint minorVersion;
    if (!eglInitialize(eglDisplay, // 创建的EGL连接
                       &majorVersion, // 返回EGL主板版本号
                       &minorVersion)) { // 返回EGL次版本号
        LOGE("eglInitialize failed: %d", eglGetError());
        return EGL_NO_DISPLAY;
    }
    return eglDisplay;
}

EGLConfig Egl::createConfig(const int *configSpec) {
    EGLint configsCount;
    const EGLint maxConfigs = 1;
    EGLConfig configs[maxConfigs];
    EGLBoolean ret = eglChooseConfig(eglDisplay, // 创建的和本地窗口系统的连接
                                     configSpec, // 指定渲染表面的参数列表，可以为null
                                     configs, // 调用成功，返会符合条件的EGLConfig列表
                                     maxConfigs, // 最多返回的符合条件的EGLConfig个数
                                     &configsCount); // 实际返回的符合条件的EGLConfig个数
    if (EGL_TRUE != ret || configsCount <= 0) {
        LOGE("eglChooseConfig failed: %d", eglGetError());
        return nullptr;
    }
    return configs[0];
}

EGLContext Egl::createContext(EGLContext context) {
    int contextSpec[] = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE};
    EGLContext eglContext = eglCreateContext(eglDisplay, eglConfig, context, contextSpec);
    if (EGL_NO_CONTEXT == eglContext) {
        LOGE("eglCreateContext failed: %d", eglGetError());
        return EGL_NO_CONTEXT;
    }
    return eglContext;
}

EGLSurface Egl::createPbufferSurface() {
//    EGLint values;
//    eglQueryContext(eglDisplay, eglContext, EGL_CONTEXT_CLIENT_VERSION, &values);
    int surfaceAttribs[] = {EGL_WIDTH, 1, EGL_HEIGHT, 1, EGL_NONE};
    EGLSurface eglSurface = eglCreatePbufferSurface(eglDisplay, eglConfig, surfaceAttribs);
    if (nullptr == eglSurface || EGL_NO_SURFACE == eglSurface) {
        LOGE("eglCreatePbufferSurface failed: %d", eglGetError());
        return EGL_NO_SURFACE;
    }
    return eglSurface;
}

EGLSurface Egl::createWindowSurface(HwWindow *win) {
    int attribList[] = {EGL_NONE};
//    EGLint values;
//    eglQueryContext(eglDisplay, eglContext, EGL_CONTEXT_CLIENT_VERSION, &values);
    EGLSurface eglSurface = eglCreateWindowSurface(eglDisplay,
                                                   eglConfig, // 选好的可用EGLConfig
                                                   win->getANativeWindow(), // 指定原生窗口
                                                   attribList); // 指定窗口属性列表，可以为null，一般指定渲染所用的缓冲区使用但缓冲或者后台缓冲，默认为后者。
    if (nullptr == eglSurface || EGL_NO_SURFACE == eglSurface) {
        LOGE("eglCreateWindowSurface failed: %d", eglGetError());
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
    if (EGL_NO_CONTEXT == eglContext) {
        LOGE("name egl failed had release!");
        return;
    }
    if (!eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
        LOGE("name makeCurrent failed: %d", eglGetError());
    }
}

void Egl::swapBuffers() {
    if (!eglSwapBuffers(eglDisplay, eglSurface)) {
        LOGE("name swapBuffers,failed!");
    }
}
