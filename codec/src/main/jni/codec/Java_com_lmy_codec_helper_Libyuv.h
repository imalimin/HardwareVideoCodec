/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <jni.h>
#include <log.h>

#ifndef HARDWAREVIDEOCODEC_JAVA_COM_LMY_CODEC_X264_LIBYUV_H
#define HARDWAREVIDEOCODEC_JAVA_COM_LMY_CODEC_X264_LIBYUV_H
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jboolean JNICALL Java_com_lmy_codec_helper_Libyuv_ConvertToI420
        (JNIEnv *, jobject, jbyteArray, jbyteArray, jint, jint, jint);

#ifdef __cplusplus
}
#endif
#endif //HARDWAREVIDEOCODEC_JAVA_COM_LMY_CODEC_X264_LIBYUV_H
