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

JNIEXPORT jlong JNICALL Java_com_lmy_hwvc_1native_processor_PictureProcessor_create
        (JNIEnv *env, jobject thiz) {
    if (!processor) {
        return reinterpret_cast<jlong>(new PictureProcessor());
    }
    return reinterpret_cast<jlong>(processor);
}

JNIEXPORT void JNICALL Java_com_lmy_hwvc_1native_processor_PictureProcessor_prepare
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

JNIEXPORT void JNICALL Java_com_lmy_hwvc_1native_processor_PictureProcessor_show
        (JNIEnv *env, jobject thiz, jlong handler, jbyteArray rgba, jint width, jint height) {
    if (handler) {
        jbyte *pData = env->GetByteArrayElements(rgba, JNI_FALSE);
        getHandler(handler)->show(reinterpret_cast<uint8_t *>(pData), width, height);
        env->ReleaseByteArrayElements(rgba, pData, 0);
    }
}

JNIEXPORT void JNICALL Java_com_lmy_hwvc_1native_processor_PictureProcessor_release
        (JNIEnv *env, jobject thiz, jlong handler) {
    if (handler) {
        PictureProcessor *p = getHandler(handler);
        delete p;
    }
}

#ifdef __cplusplus
}
#endif