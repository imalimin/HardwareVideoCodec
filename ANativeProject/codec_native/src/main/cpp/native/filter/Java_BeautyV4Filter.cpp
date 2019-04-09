/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <jni.h>
#include <log.h>
#include "BeautyV4Filter.h"

#ifdef __cplusplus
extern "C" {
#endif

static BeautyV4Filter *getHandler(jlong handler) {
    return reinterpret_cast<BeautyV4Filter *>(handler);
}

JNIEXPORT jlong JNICALL Java_com_lmy_hwvcnative_filter_BeautyV4Filter_create
        (JNIEnv *env, jobject thiz) {
    return reinterpret_cast<jlong>(new BeautyV4Filter());
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_filter_BeautyV4Filter_setParams
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