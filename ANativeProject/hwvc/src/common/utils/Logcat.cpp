//
// Created by limin on 2019/4/11.
//

#include "../include/Logcat.h"

void Logcat::i(const string &TAG, const string fmt, ...) {
#ifdef HWDEBUG
    va_list args;
    va_start(args, fmt);
#ifdef ANDROID
    __android_log_vprint(ANDROID_LOG_VERBOSE, TAG.c_str(), fmt.c_str(), args);
#else
    printf(fmt.c_str(), args);
#endif
    va_end(args);
#endif
}

void Logcat::e(const string &TAG, const string fmt, ...) {
#ifdef HWDEBUG
    va_list args;
    va_start(args, fmt);
#ifdef ANDROID
    __android_log_vprint(ANDROID_LOG_ERROR, TAG.c_str(), fmt.c_str(), args);
#else
    printf(fmt.c_str(), args);
#endif
    va_end(args);
#endif
}