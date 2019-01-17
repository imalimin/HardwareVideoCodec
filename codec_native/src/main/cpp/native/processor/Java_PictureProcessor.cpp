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

#ifdef __cplusplus
extern "C" {
#endif

static PictureProcessor *getHandler(jlong handler) {
    return reinterpret_cast<PictureProcessor *>(handler);
}

JNIEXPORT jlong JNICALL Java_com_lmy_hwvcnative_processor_PictureProcessor_create
        (JNIEnv *env, jobject thiz) {
    return reinterpret_cast<jlong>(new PictureProcessor());
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
        ANativeWindow_release(p->win);
        delete p;
    }
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_processor_PictureProcessor_setFilter
        (JNIEnv *env, jobject thiz, jlong handler, jlong filter) {
    if (handler && filter) {
        getHandler(handler)->setFilter(reinterpret_cast<Filter *>(filter));
    }
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_processor_PictureProcessor_invalidate
        (JNIEnv *env, jobject thiz, jlong handler) {
    if (handler) {
        getHandler(handler)->invalidate();
    }
}

#ifdef __cplusplus
}
#endif