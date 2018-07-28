/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "Java_com_lmy_rtmp_RtmpClient.h"
#include "RtmpClient.h"

static RtmpClient *client = NULL;
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_com_lmy_rtmp_RtmpClient_connect
        (JNIEnv *env, jobject thiz, jstring url, jint timeOut) {
    if (NULL == client) {
        client = new RtmpClient();
    }
    char *urlTmp = (char *) env->GetStringUTFChars(url, NULL);
    int ret = client->connect(urlTmp, timeOut);
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
    env->ReleaseByteArrayElements(sps, spsBuffer, JNI_FALSE);
    env->ReleaseByteArrayElements(pps, ppsBuffer, JNI_FALSE);
    return ret;
}

JNIEXPORT jint JNICALL
Java_com_lmy_rtmp_RtmpClient_sendVideo(JNIEnv *env, jobject thiz, jbyteArray data, jint len,
                                       jlong timestamp) {
    jbyte *buffer = env->GetByteArrayElements(data, JNI_FALSE);
    int ret = client->sendVideo((char *) buffer, len, timestamp);
    env->ReleaseByteArrayElements(data, buffer, JNI_FALSE);
    return ret;
}

JNIEXPORT jint JNICALL
Java_com_lmy_rtmp_RtmpClient_sendAudioSpecificData(JNIEnv *env, jobject thiz, jbyteArray data,
                                                   jint len) {
    jbyte *buffer = env->GetByteArrayElements(data, JNI_FALSE);
    int ret = client->sendAudioSpecificData((char *) buffer, len);
    env->ReleaseByteArrayElements(data, buffer, JNI_FALSE);
    return ret;
}

JNIEXPORT jint JNICALL
Java_com_lmy_rtmp_RtmpClient_sendAudio(JNIEnv *env, jobject thiz, jbyteArray data, jint len,
                                       jlong timestamp) {
    jbyte *buffer = env->GetByteArrayElements(data, JNI_FALSE);
    int ret = client->sendAudio((char *) buffer, len, timestamp);
    env->ReleaseByteArrayElements(data, buffer, JNI_FALSE);
    return ret;
}

JNIEXPORT void JNICALL
Java_com_lmy_rtmp_RtmpClient_stop(JNIEnv *env, jobject thiz) {
    client->stop();
    delete client;
    client = NULL;
}

#ifdef __cplusplus
}
#endif