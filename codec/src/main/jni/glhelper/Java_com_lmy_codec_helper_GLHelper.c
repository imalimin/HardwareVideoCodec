/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <Java_com_lmy_codec_helper_GLHelper.h>

//Log
#ifdef ANDROID

#include <jni.h>
#include <GLES2/gl2.h>
#include <cpu-features.h>
#include <string.h>

#ifdef  __ARM__
#include <arm_neon.h>
#endif

#define LOGE(format, ...)  __android_log_print(ANDROID_LOG_ERROR, "JNI", format, ##__VA_ARGS__)
#define LOGI(format, ...)  __android_log_print(ANDROID_LOG_INFO,  "JNI", format, ##__VA_ARGS__)
#else
#define LOGE(format, ...)  printf("JNI" format "\n", ##__VA_ARGS__)
#define LOGI(format, ...)  printf("JNI" format "\n", ##__VA_ARGS__)
#endif

#ifdef  __ARM__
static void neon_memcpy(volatile unsigned char *dst, volatile unsigned char *src, int sz){
    if (sz & 63)
        sz = (sz & -64) + 64;
    asm volatile (
    "NEONCopyPLD: \n"
            " VLDM %[src]!,{d0-d7} \n"
            " VSTM %[dst]!,{d0-d7} \n"
            " SUBS %[sz],%[sz],#0x40 \n"
            " BGT NEONCopyPLD \n"
    : [dst]"+r"(dst), [src]"+r"(src), [sz]"+r"(sz) : : "d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "cc", "memory");
}
#endif

JNIEXPORT void JNICALL Java_com_lmy_codec_helper_GLHelper_glReadPixels
        (JNIEnv *env, jobject thiz, jint x, jint y, jint width, jint height, jint format,
         jint type) {
    glReadPixels(x, y, width, height, format, type, 0);
}

JNIEXPORT void JNICALL Java_com_lmy_codec_helper_GLHelper_copyToByteArray
        (JNIEnv *env, jobject thiz, jobject src, jbyteArray dest, jint row, jint stride,
         jint stride_padding) {
    jbyte *srcBuffer = (*env)->GetDirectBufferAddress(env, src);
    jbyte *destBuffer = (*env)->GetByteArrayElements(env, dest, JNI_FALSE);
    int offset = 0;
    for (int i = 0; i < row; i++) {
#ifdef  __ARM__
        if (android_getCpuFamily() == ANDROID_CPU_FAMILY_ARM &&
            (android_getCpuFeatures() & ANDROID_CPU_ARM_FEATURE_NEON) != 0){//支持NEON
            neon_memcpy(destBuffer + offset, srcBuffer + offset + i * stride_padding, stride);
        }else{
            memcpy(destBuffer + offset, srcBuffer + offset + i * stride_padding, stride);
        }
#else
        memcpy(destBuffer + offset, srcBuffer + offset + i * stride_padding, stride);
#endif
        offset += stride;
    }
    (*env)->ReleaseByteArrayElements(env, dest, destBuffer, JNI_FALSE);
}

JNIEXPORT void JNICALL Java_com_lmy_codec_helper_GLHelper_memcpy
        (JNIEnv *env, jobject thiz, jbyteArray src, jbyteArray dest, jint length) {
    jbyte *srcBuffer = (*env)->GetByteArrayElements(env, src, JNI_FALSE);
    jbyte *destBuffer = (*env)->GetByteArrayElements(env, dest, JNI_FALSE);
#ifdef  __ARM__
    if (android_getCpuFamily() == ANDROID_CPU_FAMILY_ARM &&
            (android_getCpuFeatures() & ANDROID_CPU_ARM_FEATURE_NEON) != 0){//支持NEON
            neon_memcpy(destBuffer, srcBuffer, length);
        }else{
            memcpy(destBuffer, srcBuffer, length);
        }
#else
    memcpy(destBuffer, srcBuffer, length);
#endif
    (*env)->ReleaseByteArrayElements(env, src, srcBuffer, JNI_FALSE);
    (*env)->ReleaseByteArrayElements(env, dest, destBuffer, JNI_FALSE);
}
