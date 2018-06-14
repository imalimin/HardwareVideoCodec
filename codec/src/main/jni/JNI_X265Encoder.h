//
// Created by lmyooyo@gmail.com on 2018/6/13.
//
#include <jni.h>
#include <log.h>
#include <stdint.h>

#ifndef HARDWAREVIDEOCODEC_COM_LMY_CODEC_X265_X265ENCODER_H
#define HARDWAREVIDEOCODEC_COM_LMY_CODEC_X265_X265ENCODER_H
#ifdef __cplusplus
extern "C" {
#endif

void Java_com_lmy_codec_x265_X265Encoder_init
        (JNIEnv *, jobject);

void Java_com_lmy_codec_x265_X265Encoder_start
        (JNIEnv *, jobject);

void Java_com_lmy_codec_x265_X265Encoder_stop
        (JNIEnv *, jobject);

jboolean Java_com_lmy_codec_x265_X265Encoder_encode
        (JNIEnv *, jobject, jbyteArray, jbyteArray, jintArray , jintArray);

void Java_com_lmy_codec_x265_X265Encoder_setVideoSize
        (JNIEnv *, jobject, jint, jint);

void Java_com_lmy_codec_x265_X265Encoder_setBitrate
        (JNIEnv *, jobject, jint);

void Java_com_lmy_codec_x265_X265Encoder_setFrameFormat
        (JNIEnv *, jobject, jint);

void Java_com_lmy_codec_x265_X265Encoder_setFps
        (JNIEnv *, jobject, jint);

#ifdef __cplusplus
}
#endif
#endif //HARDWAREVIDEOCODEC_COM_LMY_CODEC_X265_X265ENCODER_H
