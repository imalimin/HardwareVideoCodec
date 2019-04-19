//
// Created by limin on 2019/4/11.
//

#ifndef HARDWAREVIDEOCODEC_LOGCAT_H
#define HARDWAREVIDEOCODEC_LOGCAT_H

#ifdef ANDROID

#include <android/log.h>

#endif

#include "stdio.h"
#include <string>
#include <stdarg.h>

using namespace std;

class Logcat {
public:
    static void i(const string &TAG, const string fmt, ...);

    static void e(const string &TAG, const string fmt, ...);
};


#endif //HARDWAREVIDEOCODEC_LOGCAT_H
