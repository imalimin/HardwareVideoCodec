/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_EGL_H
#define HARDWAREVIDEOCODEC_EGL_H

#include <EGL/egl.h>
#include "Object.h"

const int CONFIG_DEFAULT[] = {EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                              EGL_RED_SIZE, 8,
                              EGL_GREEN_SIZE, 8,
                              EGL_BLUE_SIZE, 8,
                              EGL_ALPHA_SIZE, 8,
                              EGL_DEPTH_SIZE, 0,
                              EGL_STENCIL_SIZE, 0,
                              EGL_NONE};

class Egl : public Object {
public:
    Egl();

    Egl(EGLContext context);

    Egl(ANativeWindow *win);

    Egl(EGLContext context, ANativeWindow *win);

    virtual ~Egl();

    void makeCurrent();

    void swapBuffers();

    int width();

    int height();

private:
    EGLDisplay eglDisplay = nullptr;
    EGLConfig eglConfig = nullptr;
    EGLContext eglContext = nullptr;
    EGLSurface eglSurface = nullptr;

    void init(EGLContext eglContext, ANativeWindow *win);

    EGLDisplay createDisplay(EGLNativeDisplayType display_id);

    EGLConfig createConfig(const int *configSpec);

    EGLContext createContext(EGLContext context);

    EGLSurface createPbufferSurface();

    EGLSurface createWindowSurface(ANativeWindow *win);

    EGLint getParams(EGLint attribute);
};


#endif //HARDWAREVIDEOCODEC_EGL_H
