/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/

#ifndef HARDWAREVIDEOCODEC_HWMEMFRAMEREF_H
#define HARDWAREVIDEOCODEC_HWMEMFRAMEREF_H

#include "HwAbsFrame.h"

class HwMemFrameRef : public HwAbsFrame {
public:
    HwMemFrameRef(uint8_t *ref, uint64_t size);

    virtual ~HwMemFrameRef();

    uint8_t *getData() override ;

    void setData(uint8_t *data, uint64_t dataSize) override ;

private:
    uint8_t *ref = nullptr;
};


#endif //HARDWAREVIDEOCODEC_HWMEMFRAMEREF_H
