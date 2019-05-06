/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/

#ifndef HARDWAREVIDEOCODEC_HWMEMFRAME_H
#define HARDWAREVIDEOCODEC_HWMEMFRAME_H

#include "Object.h"

class HwMemFrame : public Object {
public:
    HwMemFrame();

    virtual ~HwMemFrame();

    virtual uint8_t *getData();

    virtual uint64_t getDataSize();

    virtual void setData(uint8_t *data, uint64_t dataSize);

private:
    uint8_t *data = nullptr;
    uint64_t dataSize = 0;
};


#endif //HARDWAREVIDEOCODEC_HWMEMFRAME_H
