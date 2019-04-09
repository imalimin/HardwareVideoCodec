/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <jni.h>
#include <log.h>
#include "HwvcFilter.h"

#ifdef __cplusplus
extern "C" {
#endif

static HwvcFilter *getHandler(jlong handler) {
    return reinterpret_cast<HwvcFilter *>(handler);
}

JNIEXPORT jlong JNICALL Java_com_lmy_hwvcnative_filter_HwvcFilter_create
        (JNIEnv *env, jobject thiz, jstring path) {
    char *pPath = const_cast<char *>(env->GetStringUTFChars(path, NULL));
    jlong handler = reinterpret_cast<jlong>(new HwvcFilter(pPath));
    env->ReleaseStringUTFChars(path, pPath);
    return handler;
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_filter_HwvcFilter_setParams
        (JNIEnv *env, jobject thiz, jlong handler, jintArray params) {
    if (handler) {
        int *pParams = env->GetIntArrayElements(params, JNI_FALSE);
        getHandler(handler)->setParams(pParams);
        env->ReleaseIntArrayElements(params, pParams, 0);
    }
}

#ifdef __cplusplus
}
#endif