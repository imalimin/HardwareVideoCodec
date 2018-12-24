/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/Egl.h"
#include "log.h"

Egl::Egl() {
    init(nullptr, nullptr);
}

Egl::Egl(EGLContext eglContext) {
    init(eglContext, nullptr);
}

Egl::Egl(EGLContext context, ANativeWindow *win) {
    init(eglContext, win);
}

Egl::~Egl() {
    makeCurrent();
    eglDestroySurface(eglDisplay, eglSurface);
    eglDestroyContext(eglDisplay, eglContext);
    eglTerminate(eglDisplay);
    eglMakeCurrent(eglDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
    eglContext = EGL_NO_CONTEXT;
    eglDisplay = EGL_NO_DISPLAY;
    eglSurface = EGL_NO_SURFACE;
}

void Egl::init(EGLContext context, ANativeWindow *win) {
    this->eglDisplay = createDisplay(EGL_DEFAULT_DISPLAY);
    this->eglConfig = createConfig(const_cast<int *>(CONFIG_DEFAULT));
    this->eglContext = createContext(context);
    if (win) {
        this->eglSurface = createWindowSurface(win);
    } else {
        this->eglSurface = createPbufferSurface();
    }
}

EGLDisplay Egl::createDisplay(EGLNativeDisplayType display_id) {
    EGLDisplay eglDisplay = eglGetDisplay(display_id);
    if (EGL_NO_DISPLAY == eglDisplay) {
        LOGE("eglGetDisplay failed: %d", eglGetError());
        return nullptr;
    }
    if (!eglInitialize(eglDisplay, 0, 0)) {
        LOGE("eglInitialize failed: %d", eglGetError());
        return nullptr;
    }
    return eglDisplay;
}

EGLConfig Egl::createConfig(int *configSpec) {
    EGLint configsCount;
    EGLConfig configs;
    eglChooseConfig(eglDisplay, configSpec, &configs, 1, &configsCount);
    if (configsCount <= 0) {
        LOGE("eglChooseConfig failed: %d", eglGetError());
        return nullptr;
    }
    return configs;
}

EGLContext Egl::createContext(EGLContext context) {
    int contextSpec[] = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE};
    EGLContext eglContext = eglCreateContext(eglDisplay, eglConfig,
                                             context ? context : EGL_NO_CONTEXT, contextSpec);
    if (EGL_NO_CONTEXT == eglContext) {
        LOGE("eglCreateContext failed: %d", eglGetError());
        return nullptr;
    }
    return eglContext;
}

EGLSurface Egl::createPbufferSurface() {
    EGLint values;
    int surfaceAttribs[] = {EGL_WIDTH, 1, EGL_HEIGHT, 1, EGL_NONE};
    eglQueryContext(eglDisplay, eglContext, EGL_CONTEXT_CLIENT_VERSION, &values);
    EGLSurface eglSurface = eglCreatePbufferSurface(eglDisplay, eglConfig, surfaceAttribs);
    if (nullptr == eglSurface || EGL_NO_SURFACE == eglSurface) {
        LOGE("eglCreatePbufferSurface failed: %d", eglGetError());
        return nullptr;
    }
    return eglSurface;
}

EGLSurface Egl::createWindowSurface(ANativeWindow *win) {
    EGLint values;
    int surfaceAttribs[] = {EGL_NONE};
    eglQueryContext(eglDisplay, eglContext, EGL_CONTEXT_CLIENT_VERSION, &values);
    EGLSurface eglSurface = eglCreateWindowSurface(eglDisplay, eglConfig, win, surfaceAttribs);
    if (nullptr == eglSurface || EGL_NO_SURFACE == eglSurface) {
        LOGE("eglCreateWindowSurface failed: %d", eglGetError());
        return nullptr;
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
        LOGE("$name egl failed had release!");
        return;
    }
    if (!eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
        LOGE("$name makeCurrent failed: %d", eglGetError());
    }
}

void Egl::swapBuffers() {
    if (!eglSwapBuffers(eglDisplay, eglSurface)) {
        LOGE("$name swapBuffers,failed!");
    }
}
