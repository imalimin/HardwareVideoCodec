/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <jni.h>
#include <log.h>
#include "NormalFilter.h"

#ifdef __cplusplus
extern "C" {
#endif

static NormalFilter *getHandler(jlong handler) {
    return reinterpret_cast<NormalFilter *>(handler);
}

JNIEXPORT jlong JNICALL Java_com_lmy_hwvcnative_filter_NormalFilter_create
        (JNIEnv *env, jobject thiz) {
    return reinterpret_cast<jlong>(new NormalFilter());
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_filter_NormalFilter_setParams
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