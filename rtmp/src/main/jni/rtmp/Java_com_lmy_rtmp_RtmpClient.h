/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <jni.h>

#ifndef HARDWAREVIDEOCODEC_JAVA_COM_LMY_RTMP_RTMPCLIENT_H
#define HARDWAREVIDEOCODEC_JAVA_COM_LMY_RTMP_RTMPCLIENT_H

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL
Java_com_lmy_rtmp_RtmpClient_init(JNIEnv *, jobject);

JNIEXPORT jint JNICALL
Java_com_lmy_rtmp_RtmpClient_connect(JNIEnv *, jobject, jstring, jint, jint, jint);

JNIEXPORT jint JNICALL
Java_com_lmy_rtmp_RtmpClient_sendVideoSpecificData(JNIEnv *, jobject, jbyteArray, jint, jbyteArray, jint,
                                           jlong);

JNIEXPORT jint JNICALL
Java_com_lmy_rtmp_RtmpClient_sendVideo(JNIEnv *, jobject, jbyteArray, jint, jlong);

JNIEXPORT jint JNICALL
Java_com_lmy_rtmp_RtmpClient_sendAudioSpecificData(JNIEnv *, jobject, jbyteArray, jint);

JNIEXPORT jint JNICALL
Java_com_lmy_rtmp_RtmpClient_sendAudio(JNIEnv *, jobject, jbyteArray, jint, jlong);

JNIEXPORT void JNICALL
Java_com_lmy_rtmp_RtmpClient_stop(JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif


#endif //HARDWAREVIDEOCODEC_JAVA_COM_LMY_RTMP_RTMPCLIENT_H
