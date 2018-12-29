/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <jni.h>
#include <log.h>
#include "PinkFilter.h"
#include "../include/StringUtils.h"

#ifdef __cplusplus
extern "C" {
#endif

static PinkFilter *getHandler(jlong handler) {
    return reinterpret_cast<PinkFilter *>(handler);
}

JNIEXPORT jlong JNICALL Java_com_lmy_hwvcnative_filter_PinkFilter_create
        (JNIEnv *env, jobject thiz, jobjectArray names, jobjectArray samplers) {
    string *nameArray;
    int size = StringUtils::jStringArray2StringArray(env, names, &nameArray);
    for (int i = 0; i < size; ++i) {
        LOGI("%s", nameArray[i].c_str());
    }

    string *samplerArray;
    StringUtils::jStringArray2StringArray(env, samplers, &samplerArray);

    return reinterpret_cast<jlong>(new PinkFilter(nameArray, samplerArray, size));
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_filter_PinkFilter_setParams
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