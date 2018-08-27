/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "Java_com_lmy_rtmp_RtmpClient.h"

static RtmpClient *client = NULL;
static JavaVM *globalVM;
static bool detached = false;
static jobject obj;
static jmethodID errorCallbackMethod;
#ifdef __cplusplus
extern "C" {
#endif

static void attach(JavaVM *vm) {
    if (client->getPipeline()) {
        if (detached)
            return;
        JNIEnv *env;
        int ret = vm->GetEnv((void **) &env, JNI_VERSION_1_6);
        if (ret < 0) {
            ret = vm->AttachCurrentThread(&env, NULL);
            if (ret < 0) {
                LOGE("Cannot attach current thread.");
                return;
            }
        }
        detached = true;
    }
}

static void handleMessage(Message *msg) {
    if (NULL == msg || NULL == msg->obj) return;
    JavaVMWrapper *wrapper = (JavaVMWrapper *) msg->obj;
    if (detached)
        wrapper->vm->DetachCurrentThread();
    LOGI("DetachCurrentThread");
}

static void detach(RtmpClient *client, JavaVM *vm) {
    if (client->getPipeline()) {
        client->getPipeline()->sendMessage(
                obtainMessage(0, new JavaVMWrapper(vm), handleMessage));
    }
}

void onError(int error) {
    if (NULL == globalVM || NULL == obj || NULL == errorCallbackMethod) {
        LOGE("Please call setupErrorCallback before use.");
        return;
    }
    LOGE("onError %d.", error);
    attach(globalVM);
    JNIEnv *env;
    globalVM->GetEnv((void **) &env, JNI_VERSION_1_6);
    env->CallVoidMethod(obj, errorCallbackMethod, error);
}

static bool setupErrorCallback(JNIEnv *env, jobject thiz) {
    env->GetJavaVM(&globalVM);
    jclass clazz = env->GetObjectClass(thiz);
    if (NULL == clazz) {
        LOGE("Cannot find class.");
        return false;
    }
    obj = env->NewGlobalRef(thiz);
    if (NULL == obj) {
        LOGE("Cannot ref object.");
        return false;
    }
    errorCallbackMethod = env->GetMethodID(clazz, "onJniError", "(I)V");
    if (NULL == errorCallbackMethod) {
        LOGE("Cannot get method.");
        obj = NULL;
        return false;
    }
    return true;
}

JNIEXPORT jint JNICALL Java_com_lmy_rtmp_RtmpClient_connect
        (JNIEnv *env, jobject thiz, jstring url, jint timeOutMs, jint cacheSize) {
    if (NULL == client) {
        setupErrorCallback(env, thiz);
        client = new RtmpClient(cacheSize);
        client->setErrorCallback(onError);
    }
    char *urlTmp = (char *) env->GetStringUTFChars(url, NULL);
    int ret = client->connect(urlTmp, timeOutMs);
    env->ReleaseStringUTFChars(url, urlTmp);
    return ret;
}

JNIEXPORT jint JNICALL Java_com_lmy_rtmp_RtmpClient_connectStream
        (JNIEnv *env, jobject thiz, jint width, jint height) {
    return client->connectStream(width, height);
}

JNIEXPORT jint JNICALL
Java_com_lmy_rtmp_RtmpClient_sendVideoSpecificData(JNIEnv *env, jobject thiz, jbyteArray sps,
                                                   jint spsLen,
                                                   jbyteArray pps, jint ppsLen) {
    jbyte *spsBuffer = env->GetByteArrayElements(sps, JNI_FALSE);
    jbyte *ppsBuffer = env->GetByteArrayElements(pps, JNI_FALSE);
    int ret = client->sendVideoSpecificData((char *) spsBuffer, spsLen, (char *) ppsBuffer, ppsLen);
    env->ReleaseByteArrayElements(sps, spsBuffer, NULL);
    env->ReleaseByteArrayElements(pps, ppsBuffer, NULL);
    return ret;
}

JNIEXPORT jint JNICALL
Java_com_lmy_rtmp_RtmpClient_sendVideo(JNIEnv *env, jobject thiz, jbyteArray data, jint len,
                                       jlong timestamp) {
    jbyte *buffer = env->GetByteArrayElements(data, JNI_FALSE);
    int ret = client->sendVideo((char *) buffer, len, timestamp);
    env->ReleaseByteArrayElements(data, buffer, NULL);
    return ret;
}

JNIEXPORT jint JNICALL
Java_com_lmy_rtmp_RtmpClient_sendAudioSpecificData(JNIEnv *env, jobject thiz, jbyteArray data,
                                                   jint len) {
    jbyte *buffer = env->GetByteArrayElements(data, JNI_FALSE);
    int ret = client->sendAudioSpecificData((char *) buffer, len);
    env->ReleaseByteArrayElements(data, buffer, NULL);
    return ret;
}

JNIEXPORT jint JNICALL
Java_com_lmy_rtmp_RtmpClient_sendAudio(JNIEnv *env, jobject thiz, jbyteArray data, jint len,
                                       jlong timestamp) {
    jbyte *buffer = env->GetByteArrayElements(data, JNI_FALSE);
    int ret = client->sendAudio((char *) buffer, len, timestamp);
    env->ReleaseByteArrayElements(data, buffer, NULL);
    return ret;
}

JNIEXPORT void JNICALL
Java_com_lmy_rtmp_RtmpClient_stop(JNIEnv *e, jobject thiz) {
    errorCallbackMethod = NULL;
    if (NULL != obj) {
        e->DeleteGlobalRef(obj);
        obj = NULL;
    }
    if (NULL != client) {
        detach(client, globalVM);
        client->stop();
        delete client;
        client = NULL;
    }
    globalVM = NULL;
}
JNIEXPORT void JNICALL
Java_com_lmy_rtmp_RtmpClient_setCacheSize(JNIEnv *env, jobject thiz, jint size) {
    if (NULL != client) {
        client->setCacheSize(size);
    }
}

#ifdef __cplusplus
}
#endif