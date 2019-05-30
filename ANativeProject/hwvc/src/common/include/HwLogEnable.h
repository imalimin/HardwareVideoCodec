/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/

#ifndef HARDWAREVIDEOCODEC_HWLOGENABLE_H
#define HARDWAREVIDEOCODEC_HWLOGENABLE_H

#include "Object.h"

class HwLogEnable : public Object {
public:
    HwLogEnable();

    virtual ~HwLogEnable();

    void setLogEnable(bool enable);

    bool isLogEnable();

private:
    bool logEnable = false;
};


#endif //HARDWAREVIDEOCODEC_HWLOGENABLE_H
