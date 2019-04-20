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
            checkError();
            LOGE("~Egl makeCurrent failed");
        }
        if (eglContext != EGL_NO_CONTEXT) {
            eglDestroyContext(eglDisplay, eglContext);
        }
        if (eglSurface != EGL_NO_SURFACE) {
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
    if (context) {
        createContext(context->eglContext);
    } else {
        createContext(EGL_NO_CONTEXT);
    }
    if (EGL_NO_CONTEXT == this->eglContext) {
        LOGE("$s bad context", __func__);
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
    makeCurrent();
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
    const EGLint maxConfigs = 1;
    EGLConfig configs[maxConfigs];
    EGLBoolean ret = eglChooseConfig(eglDisplay, // 创建的和本地窗口系统的连接
                                     configSpec, // 指定渲染表面的参数列表，可以为null
                                     configs, // 调用成功，返会符合条件的EGLConfig列表
                                     maxConfigs, // 最多返回的符合条件的EGLConfig个数
                                     &configsCount); // 实际返回的符合条件的EGLConfig个数
    if (EGL_TRUE != ret || configsCount <= 0 || !checkError()) {
        LOGE("eglChooseConfig failed");
        return nullptr;
    }
    eglConfig = configs[0];
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
    EGLint values;
    eglQueryContext(eglDisplay, eglContext, EGL_CONTEXT_CLIENT_VERSION, &values);
    int surfaceAttribs[] = {EGL_WIDTH, 1, EGL_HEIGHT, 1, EGL_NONE};
    eglSurface = eglCreatePbufferSurface(eglDisplay, eglConfig, surfaceAttribs);
    if (nullptr == eglSurface || EGL_NO_SURFACE == eglSurface || !checkError()) {
        LOGE("eglCreatePbufferSurface failed");
        return EGL_NO_SURFACE;
    }
    return eglSurface;
}

EGLSurface Egl::createWindowSurface(HwWindow *win) {
    int attribList[] = {EGL_NONE};
    EGLint values;
    eglQueryContext(eglDisplay, eglContext, EGL_CONTEXT_CLIENT_VERSION, &values);
    eglSurface = eglCreateWindowSurface(eglDisplay,
                                        eglConfig, // 选好的可用EGLConfig
                                        win->getANativeWindow(), // 指定原生窗口
                                        attribList); // 指定窗口属性列表，可以为null，一般指定渲染所用的缓冲区使用但缓冲或者后台缓冲，默认为后者。
    if (nullptr == eglSurface || EGL_NO_SURFACE == eglSurface || !checkError()) {
        LOGE("eglCreateWindowSurface failed");
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
    LOGI("Egl makeCurrent(%ld), %ld, %ld, %ld, %ld", pthread_self(), eglDisplay, eglConfig,
         eglContext, eglSurface);
    if (!eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext) || !checkError()) {
        LOGE("name makeCurrent failed");
    }
}

void Egl::swapBuffers() {
    if (!eglSwapBuffers(eglDisplay, eglSurface)) {
        LOGE("name swapBuffers,failed!");
    }
}

bool Egl::checkError() {
    LOGE("Egl::checkError");
    EGLint error = eglGetError();
    if (EGL_SUCCESS != error) {
        LOGE("Bad EGL environment: %d", error);
        return false;
    }
    return true;
}
