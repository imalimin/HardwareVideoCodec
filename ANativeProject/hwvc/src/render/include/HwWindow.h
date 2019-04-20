/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_HWWINDOW_H
#define HARDWAREVIDEOCODEC_HWWINDOW_H

#include "Object.h"
#include <EGL/egl.h>

class HwWindow : public Object {
public:
    HwWindow();

    virtual ~HwWindow();

    NativeWindowType getANativeWindow();

    void setANativeWindow(NativeWindowType win);

private:
    NativeWindowType win = nullptr;
};

#endif //HARDWAREVIDEOCODEC_HWWINDOW_H
