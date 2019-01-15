/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/
#include <jni.h>
#include <log.h>
#include "VideoProcessor.h"
#include <android/native_window_jni.h>

#ifdef __cplusplus
extern "C" {
#endif

#include "ff/libavcodec/jni.h"

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    av_jni_set_java_vm(vm, NULL);
    return JNI_VERSION_1_6;
}

static VideoProcessor *getHandler(jlong handler) {
    return reinterpret_cast<VideoProcessor *>(handler);
}

JNIEXPORT jlong JNICALL Java_com_lmy_hwvcnative_processor_VideoProcessor_create
        (JNIEnv *env, jobject thiz) {
    return reinterpret_cast<jlong>(new VideoProcessor());
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_processor_VideoProcessor_setSource
        (JNIEnv *env, jobject thiz, jlong handler, jstring path) {
    if (handler) {
        int len = env->GetStringUTFLength(path) + 1;
        const char *pPath = env->GetStringUTFChars(path, JNI_FALSE);
        char *str = new char[len];
        memcpy(str, pPath, len);
        getHandler(handler)->setSource(str);
        env->ReleaseStringUTFChars(path, pPath);
    }
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_processor_VideoProcessor_prepare
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

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_processor_VideoProcessor_start
        (JNIEnv *env, jobject thiz, jlong handler) {
    if (handler) {
        getHandler(handler)->start();
    }
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_processor_VideoProcessor_setFilter
        (JNIEnv *env, jobject thiz, jlong handler, jlong filter) {
    if (handler && filter) {
        getHandler(handler)->setFilter(reinterpret_cast<Filter *>(filter));
    }
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_processor_VideoProcessor_release
        (JNIEnv *env, jobject thiz, jlong handler) {
    if (handler) {
        VideoProcessor *p = getHandler(handler);
        delete p;
    }
}

#ifdef __cplusplus
}
#endif