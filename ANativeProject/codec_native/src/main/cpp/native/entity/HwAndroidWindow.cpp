/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */

#include "../include/HwAndroidWindow.h"
#include "log.h"

HwAndroidWindow::HwAndroidWindow() : HwWindow() {

}

HwAndroidWindow::HwAndroidWindow(JNIEnv *env, jobject surface) {
    setANativeWindow(ANativeWindow_fromSurface(env, surface));
    if (!getANativeWindow()) {
        LOGE("ANativeWindow_fromSurface failed");
        return;
    }
}

HwAndroidWindow::~HwAndroidWindow() {
    if (getANativeWindow()) {
        ANativeWindow_release(reinterpret_cast<ANativeWindow *>(getANativeWindow()));
    }
}