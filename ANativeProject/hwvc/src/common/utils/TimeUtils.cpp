/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/
#include "../include/TimeUtils.h"

long long getCurrentTimeUS() {
    struct timeval tv;
    gettimeofday(&tv, nullptr);
    return static_cast<long long int>(tv.tv_sec * 1000000.0 + tv.tv_usec);
}