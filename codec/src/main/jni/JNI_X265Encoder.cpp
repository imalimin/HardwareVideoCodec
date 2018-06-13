/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <JNI_X265Encoder.h>
#include <X265Encoder.h>

static X265Encoder *encoder;
#ifdef __cplusplus
extern "C" {
#endif

static bool encode(jbyte *src, jbyte *dest, jint size, jint type) {
    return encoder->encode((char *) src, (char *) dest, &size, &type);
}

void Java_com_lmy_codec_x265_X265Encoder_init
        (JNIEnv *env, jobject thiz) {
    encoder = new X265Encoder();
}

void Java_com_lmy_codec_x265_X265Encoder_start
        (JNIEnv *env, jobject thiz) {
    encoder->start();
}

void Java_com_lmy_codec_x265_X265Encoder_stop
        (JNIEnv *env, jobject thiz) {
    encoder->stop();
    delete encoder;
    encoder = NULL;
}

jboolean Java_com_lmy_codec_x265_X265Encoder_encode
        (JNIEnv *env, jobject thiz, jbyteArray src, jbyteArray dest, jint size, jint type) {
    jbyte *srcBuffer = env->GetByteArrayElements(src, 0);
    jbyte *destBuffer = env->GetByteArrayElements(dest, 0);
    bool result = encode(srcBuffer, destBuffer, size, type);
    env->ReleaseByteArrayElements(src, srcBuffer, 0);
    env->ReleaseByteArrayElements(dest, destBuffer, 0);
    return (jboolean) result;
}

void Java_com_lmy_codec_x265_X265Encoder_setVideoSize
        (JNIEnv *env, jobject thiz, jint width, jint height) {
    encoder->setVideoSize(width, height);
}

void Java_com_lmy_codec_x265_X265Encoder_setBitrate
        (JNIEnv *env, jobject thiz, jint bitrate) {
    encoder->setBitrate(bitrate);
}

void Java_com_lmy_codec_x265_X265Encoder_setFrameFormat
        (JNIEnv *env, jobject thiz, jint format) {
    encoder->setFrameFormat(format);
}

void Java_com_lmy_codec_x265_X265Encoder_setFps
        (JNIEnv *env, jobject thiz, jint fps) {
    encoder->setFps(fps);
}

static JNINativeMethod methods[] = {
        {"init",           "(V)V",      (void *) Java_com_lmy_codec_x265_X265Encoder_init},
        {"start",          "(V)V",      (void *) Java_com_lmy_codec_x265_X265Encoder_start},
        {"stop",           "(V)V",      (void *) Java_com_lmy_codec_x265_X265Encoder_stop},
        {"encode",         "([B[BII)Z", (void *) Java_com_lmy_codec_x265_X265Encoder_encode},
        {"setVideoSize",   "(II)V",     (void *) Java_com_lmy_codec_x265_X265Encoder_setVideoSize},
        {"setBitrate",     "(I)V",      (void *) Java_com_lmy_codec_x265_X265Encoder_setBitrate},
        {"setFrameFormat", "(I)V",      (void *) Java_com_lmy_codec_x265_X265Encoder_setFrameFormat},
        {"setFps",         "(I)V",      (void *) Java_com_lmy_codec_x265_X265Encoder_setFps}
};

static const char *classPathName = "com/lmy/codec/x265/X265Encoder";

static int registerNativeMethods(JNIEnv *env, const char *className,
                                 JNINativeMethod *gMethods, int numMethods) {
    jclass clazz;

    clazz = env->FindClass(className);
    if (clazz == NULL) {
        LOGE("Native registration unable to find class '%s'", className);
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        LOGE("RegisterNatives failed for '%s'", className);
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

static int registerNatives(JNIEnv *env) {
    if (!registerNativeMethods(env, classPathName,
                               methods, sizeof(methods) / sizeof(methods[0]))) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

typedef union {
    JNIEnv *env;
    void *venv;
} UnionJNIEnvToVoid;

/* This function will be call when the library first be loaded */
jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    UnionJNIEnvToVoid uenv;
    JNIEnv *env = NULL;
    //LOGI("JNI_OnLoad!");

    if (vm->GetEnv((void **) &uenv.venv, JNI_VERSION_1_4) != JNI_OK) {
        //LOGE("ERROR: GetEnv failed");
        return -1;
    }

    env = uenv.env;;

    //jniRegisterNativeMethods(env, "whf/jnitest/Person", methods, sizeof(methods) / sizeof(methods[0]));

    if (registerNatives(env) != JNI_TRUE) {
        //LOGE("ERROR: registerNatives failed");
        return -1;
    }

    return JNI_VERSION_1_4;
}

#ifdef __cplusplus
}
#endif