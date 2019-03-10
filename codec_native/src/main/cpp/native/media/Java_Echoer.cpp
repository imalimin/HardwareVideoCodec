/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <jni.h>
#include <log.h>
#include "Echoer.h"

#ifdef __cplusplus
extern "C" {
#endif

static Echoer *getHandler(jlong handler) {
    return reinterpret_cast<Echoer *>(handler);
}

JNIEXPORT jlong JNICALL Java_com_lmy_hwvcnative_media_Echoer_create
        (JNIEnv *env, jobject thiz, jint channels, jint sampleHz, jint format, jint minBufferSize) {
    return reinterpret_cast<jlong>(new Echoer(channels, sampleHz, format, minBufferSize));
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_media_Echoer_start
        (JNIEnv *env, jobject thiz, jlong handler) {
    if (handler) {
        getHandler(handler)->start();
    }
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_media_Echoer_stop
        (JNIEnv *env, jobject thiz, jlong handler) {
    if (handler) {
        getHandler(handler)->stop();
    }
}

#ifdef __cplusplus
}
#endif