//
// Created by limin on 2018/7/28.
//

#include "Java_com_lmy_codec_native_processor_DeNoise.h"
#include "DeNoise.h"

#ifdef __cplusplus
extern "C" {
#endif
static DeNoise *processor = NULL;

JNIEXPORT void JNICALL
Java_com_lmy_codec_native_processor_DeNoise_start(JNIEnv *env, jobject thiz, jint sampleRate,
                                                  jint sampleSize) {
    if (NULL == processor) {
        processor = new DeNoise(sampleRate, sampleSize);
    }
}

JNIEXPORT int JNICALL
Java_com_lmy_codec_native_processor_DeNoise_preprocess(JNIEnv *env, jobject thiz, jbyteArray data) {
    jbyte *dataTmp = env->GetByteArrayElements(data, JNI_FALSE);
    int ret = processor->preprocess((char *) dataTmp);
    env->ReleaseByteArrayElements(data, dataTmp, JNI_FALSE);
    return ret;
}

JNIEXPORT void JNICALL
Java_com_lmy_codec_native_processor_DeNoise_stop(JNIEnv *env, jobject thiz) {
    if (NULL == processor) {
        delete processor;
    }
}

#ifdef __cplusplus
}
#endif