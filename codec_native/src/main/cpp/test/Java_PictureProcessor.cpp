//
// Created by mingyi.li on 2018/12/25.
//
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

JNIEXPORT jlong JNICALL Java_com_lmy_samplenative_processor_PictureProcessor_create
        (JNIEnv *env, jobject thiz) {
    if (!processor) {
        return reinterpret_cast<jlong>(new PictureProcessor());
    }
    return reinterpret_cast<jlong>(processor);
}

JNIEXPORT void JNICALL Java_com_lmy_samplenative_processor_PictureProcessor_prepare
        (JNIEnv *env, jobject thiz, jlong handler, jobject surface) {
    if (handler) {
        ANativeWindow *win = ANativeWindow_fromSurface(env, surface);
        if (!win) {
            LOGE("ANativeWindow_fromSurface failed");
            return;
        }
        getHandler(handler)->prepare(win);
    }
}

#ifdef __cplusplus
}
#endif