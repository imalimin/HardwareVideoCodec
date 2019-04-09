/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <jni.h>
#include <log.h>
//#include "FilterReader.h"
#include <android/native_window_jni.h>

#ifdef __cplusplus
extern "C" {
#endif

//static FilterReader *getHandler(jlong handler) {
//    return reinterpret_cast<FilterReader *>(handler);
//}
//
//JNIEXPORT jlong JNICALL Java_com_lmy_hwvcnative_FilterReader_create
//        (JNIEnv *env, jobject thiz, jstring file) {
//    int len = env->GetStringUTFLength(file) + 1;
//    const char *pFile = env->GetStringUTFChars(file, NULL);
//    char *path = new char[len];
//    memcpy(path, pFile, len);
//    jlong handler = reinterpret_cast<jlong>(new FilterReader(path));
//    env->ReleaseStringUTFChars(file, pFile);
//    return handler;
//}
//
//JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_FilterReader_read
//        (JNIEnv *env, jobject thiz, jlong handler) {
//    if (handler) {
//        getHandler(handler)->read();
//    }
//}

#ifdef __cplusplus
}
#endif