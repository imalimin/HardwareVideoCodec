/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/
#include <jni.h>
#include <log.h>
#include <string>
#include "AudioProcessor.h"

using namespace std;

#ifdef __cplusplus
extern "C" {
#endif

static AudioProcessor *getHandler(jlong handler) {
    return reinterpret_cast<AudioProcessor *>(handler);
}

JNIEXPORT jlong JNICALL Java_com_lmy_hwvcnative_processor_AudioProcessor_create
        (JNIEnv *env, jobject thiz) {
    return reinterpret_cast<jlong>(new AudioProcessor());
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_processor_AudioProcessor_setSource
        (JNIEnv *env, jobject thiz, jlong handler, jstring path) {
    if (handler) {
        int len = env->GetStringUTFLength(path) + 1;
        const char *pPath = env->GetStringUTFChars(path, JNI_FALSE);
        std::string pathStr(pPath);
        getHandler(handler)->setSource(&pathStr);
        env->ReleaseStringUTFChars(path, pPath);
    }
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_processor_AudioProcessor_prepare
        (JNIEnv *env, jobject thiz, jlong handler) {
    if (handler) {
        getHandler(handler)->prepare();
    }
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_processor_AudioProcessor_start
        (JNIEnv *env, jobject thiz, jlong handler) {
    if (handler) {
        getHandler(handler)->start();
    }
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_processor_AudioProcessor_pause
        (JNIEnv *env, jobject thiz, jlong handler) {
    if (handler) {
        getHandler(handler)->pause();
    }
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_processor_AudioProcessor_stop
        (JNIEnv *env, jobject thiz, jlong handler) {
    if (handler) {
        getHandler(handler)->stop();
    }
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_processor_AudioProcessor_seek
        (JNIEnv *env, jobject thiz, jlong handler, jlong us) {
    if (handler) {
        getHandler(handler)->seek(us);
    }
}

JNIEXPORT void JNICALL Java_com_lmy_hwvcnative_processor_AudioProcessor_release
        (JNIEnv *env, jobject thiz, jlong handler) {
    if (handler) {
        AudioProcessor *p = getHandler(handler);
        delete p;
    }
}

#ifdef __cplusplus
}
#endif
