/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
//
// Created by limin on 2018/9/15.
//
#include "Java_com_lmy_codec_helper_Libyuv.h"
#include "libyuv.h"

#ifdef __cplusplus
extern "C" {
#endif

static libyuv::RotationMode getRotation(int rotation) {
    switch (rotation) {
        case 0:
            return libyuv::kRotate0;
        case 1:
            return libyuv::kRotate90;
        case 2:
            return libyuv::kRotate180;
        case 3:
            return libyuv::kRotate270;
    }
    return libyuv::kRotate0;
}
JNIEXPORT jboolean JNICALL Java_com_lmy_codec_helper_Libyuv_ConvertToI420
        (JNIEnv *env, jobject thiz, jbyteArray src, jbyteArray dest, jint width, jint height,
         jint rotation) {
    jbyte *srcBuffer = env->GetByteArrayElements(src, JNI_FALSE);
    jbyte *destBuffer = env->GetByteArrayElements(dest, JNI_FALSE);
    int y_size = width * height;
    uint8 *y = reinterpret_cast<uint8 *>(destBuffer);
    uint8 *u = y + y_size;
    uint8 *v = y + y_size * 5 / 4;
    int ret = libyuv::ConvertToI420((const uint8 *) srcBuffer, width * height,
                                    y, width,
                                    u, width / 2,
                                    v, width / 2,
                                    0, 0,
                                    width, height,
                                    width, height,
                                    getRotation(rotation), libyuv::FOURCC_ABGR);
    env->ReleaseByteArrayElements(src, srcBuffer, JNI_FALSE);
    env->ReleaseByteArrayElements(dest, destBuffer, JNI_FALSE);
    return static_cast<jboolean>(ret >= 0);
}

#ifdef __cplusplus
}
#endif