//
// Created by limin on 2019/5/6.
//

#ifndef HARDWAREVIDEOCODEC_HWMEMFRAME_H
#define HARDWAREVIDEOCODEC_HWMEMFRAME_H

#include "Object.h"

class HwMemFrame : public Object {
public:
    HwMemFrame();

    virtual ~HwMemFrame();

    uint8_t *getData();

    uint64_t getDataSize();

    void setData(uint8_t *data, uint64_t dataSize);

private:
    uint8_t *data = nullptr;
    uint64_t dataSize = 0;
};


#endif //HARDWAREVIDEOCODEC_HWMEMFRAME_H
