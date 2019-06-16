/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/

#ifndef HARDWAREVIDEOCODEC_HWABSFRAME_H
#define HARDWAREVIDEOCODEC_HWABSFRAME_H

#include "Object.h"

class HwAbsFrame : virtual public Object {
public:
    HwAbsFrame();

    virtual ~HwAbsFrame();

    virtual uint8_t *getData();

    virtual uint64_t getDataSize();

    virtual void setData(uint8_t *data, uint64_t dataSize);

private:
    uint8_t *data = nullptr;
    uint64_t dataSize = 0;
};


#endif //HARDWAREVIDEOCODEC_HWABSFRAME_H
