/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <Java_com_lmy_codec_x265_X265Encoder.h>
#include <X265Encoder.h>

static X265Encoder *encoder;
#ifdef __cplusplus
extern "C" {
#endif

static bool encode(jbyte *src, jbyte *dest, int *size, int *type) {
    return encoder->encode((char *) src, (char *) dest, size, type);
}

JNIEXPORT void JNICALL Java_com_lmy_codec_x265_X265Encoder_init
        (JNIEnv *env, jobject thiz) {
    encoder = new X265Encoder();
}

JNIEXPORT void JNICALL Java_com_lmy_codec_x265_X265Encoder_start
        (JNIEnv *env, jobject thiz) {
    encoder->start();
}

JNIEXPORT void JNICALL Java_com_lmy_codec_x265_X265Encoder_stop
        (JNIEnv *env, jobject thiz) {
    encoder->stop();
    delete encoder;
    encoder = NULL;
}

JNIEXPORT jboolean JNICALL Java_com_lmy_codec_x265_X265Encoder_encode
        (JNIEnv *env, jobject thiz, jbyteArray src, jbyteArray dest, jintArray size,
         jintArray type) {
    jbyte *srcBuffer = env->GetByteArrayElements(src, JNI_FALSE);
    jbyte *destBuffer = env->GetByteArrayElements(dest, JNI_FALSE);
    jint *pSize = env->GetIntArrayElements(size, JNI_FALSE);
    jint *pType = env->GetIntArrayElements(type, JNI_FALSE);
    bool result = encode(srcBuffer, destBuffer, pSize, pType);
    env->ReleaseByteArrayElements(src, srcBuffer, JNI_FALSE);
    env->ReleaseByteArrayElements(dest, destBuffer, JNI_FALSE);
    env->ReleaseIntArrayElements(size, pSize, JNI_FALSE);
    env->ReleaseIntArrayElements(type, pType, JNI_FALSE);
    return (jboolean) result;
}

JNIEXPORT void JNICALL Java_com_lmy_codec_x265_X265Encoder_setVideoSize
        (JNIEnv *env, jobject thiz, jint width, jint height) {
    encoder->setVideoSize(width, height);
}

JNIEXPORT void JNICALL Java_com_lmy_codec_x265_X265Encoder_setBitrate
        (JNIEnv *env, jobject thiz, jint bitrate) {
    encoder->setBitrate(bitrate);
}

JNIEXPORT void JNICALL Java_com_lmy_codec_x265_X265Encoder_setFrameFormat
        (JNIEnv *env, jobject thiz, jint format) {
    encoder->setFrameFormat(format);
}

JNIEXPORT void JNICALL Java_com_lmy_codec_x265_X265Encoder_setFps
        (JNIEnv *env, jobject thiz, jint fps) {
    encoder->setFps(fps);
}

#ifdef __cplusplus
}
#endif