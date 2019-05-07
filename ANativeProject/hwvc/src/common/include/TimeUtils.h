/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/
#ifndef HARDWAREVIDEOCODEC_TIMEUTILS_H
#define HARDWAREVIDEOCODEC_TIMEUTILS_H

#include <sys/time.h>
#include "Object.h"

class TimeUtils : public Object {
public:

    ~TimeUtils();

    static int64_t getCurrentTimeUS();

private:
    TimeUtils();
};

int64_t getCurrentTimeUS();

#endif //HARDWAREVIDEOCODEC_TIMEUTILS_H
