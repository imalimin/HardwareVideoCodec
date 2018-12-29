/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <jni.h>
#include <log.h>
#include "PictureProcessor.h"
#include <android/native_window_jni.h>

PictureProcessor *processor = nullptr;
#ifdef __cplusplus
extern "C" {
#endif

static PictureProcessor *getHandler(jlong handler) {
    return reinterpret_cast<PictureProcessor *>(handler);
}

JNIEXPORT jlong JNICALL Java_com_lmy_hwvcnative_processor_PictureProcessor_create
        (JNIEnv *env, jobject thiz) {
    if (!processor) {
        return reinterpret_cast<jlong>(new PictureProcessor());
    }
    return reinterpret_cast<jlong>(processor);
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_processor_PictureProcessor_prepare
        (JNIEnv *env, jobject thiz, jlong handler, jobject surface, jint width, jint height) {
    if (handler) {
        ANativeWindow *win = ANativeWindow_fromSurface(env, surface);
        if (!win) {
            LOGE("ANativeWindow_fromSurface failed");
            return;
        }
        getHandler(handler)->prepare(win, width, height);
    }
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_processor_PictureProcessor_show
        (JNIEnv *env, jobject thiz, jlong handler, jstring file) {
    if (handler) {
        int len = env->GetStringUTFLength(file) + 1;
        const char *pFile = env->GetStringUTFChars(file, JNI_FALSE);
        char *path = new char[len];
        memcpy(path, pFile, len);
        getHandler(handler)->show(path);
        env->ReleaseStringUTFChars(file, pFile);
    }
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_processor_PictureProcessor_release
        (JNIEnv *env, jobject thiz, jlong handler) {
    if (handler) {
        PictureProcessor *p = getHandler(handler);
        delete p;
    }
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_processor_PictureProcessor_setFilterParams
        (JNIEnv *env, jobject thiz, jlong handler, jintArray params) {
    if (handler) {
        int *pParams = env->GetIntArrayElements(params, JNI_FALSE);
        getHandler(handler)->setFilterParams(pParams);
        env->ReleaseIntArrayElements(params, pParams, 0);
    }
}

#ifdef __cplusplus
}
#endif