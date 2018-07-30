/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <jni.h>
#include <android/log.h>
#include <stdint.h>

#ifndef HARDWAREVIDEOCODEC_JAVA_COM_LMY_CODEC_NATIVE_PROCESSER_DENOISE_H
#define HARDWAREVIDEOCODEC_JAVA_COM_LMY_CODEC_NATIVE_PROCESSER_DENOISE_H

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL
Java_com_lmy_codec_native_processor_DeNoise_start(JNIEnv *, jobject, jint, jint);

JNIEXPORT int JNICALL
Java_com_lmy_codec_native_processor_DeNoise_preprocess(JNIEnv *, jobject, jbyteArray);

JNIEXPORT void JNICALL
Java_com_lmy_codec_native_processor_DeNoise_stop(JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif


#endif //HARDWAREVIDEOCODEC_JAVA_COM_LMY_CODEC_NATIVE_PROCESSER_DENOISE_H
