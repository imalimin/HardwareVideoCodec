//
// Created by nonolive66 on 2018/6/13.
//

#ifdef ANDROID

#include <jni.h>
#include <android/log.h>

#define LOGE(format, ...)  __android_log_print(ANDROID_LOG_ERROR, "JNI", format, ##__VA_ARGS__)
#define LOGI(format, ...)  __android_log_print(ANDROID_LOG_INFO,  "JNI", format, ##__VA_ARGS__)
#else
#define LOGE(format, ...)  printf("JNI" format "\n", ##__VA_ARGS__)
#define LOGI(format, ...)  printf("JNI" format "\n", ##__VA_ARGS__)
#endif

#ifndef HARDWAREVIDEOCODEC_LOG_H
#define HARDWAREVIDEOCODEC_LOG_H

#endif //HARDWAREVIDEOCODEC_LOG_H
