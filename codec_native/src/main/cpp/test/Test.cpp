/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <jni.h>
#include <log.h>
#include "Render.h"

Render *render = nullptr;
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_com_lmy_samplenative_MainActivity_addMessage
        (JNIEnv *env, jobject thiz) {
    if (nullptr == render) {
        render = new Render();
    }
//    render->post();
}
JNIEXPORT void JNICALL Java_com_lmy_samplenative_MainActivity_stop
        (JNIEnv *env, jobject thiz) {
    if (nullptr != render) {
        delete render;
        render = nullptr;
    }
}

#ifdef __cplusplus
}
#endif
