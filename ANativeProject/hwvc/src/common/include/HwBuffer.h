/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/

#include "Object.h"

#ifndef HARDWAREVIDEOCODEC_HWBUFFER_H
#define HARDWAREVIDEOCODEC_HWBUFFER_H


class HwBuffer : public Object {
public:
    static HwBuffer *alloc(size_t size);

    static HwBuffer *wrap(uint8_t *buf, size_t size);

private:
    HwBuffer(size_t size);

    HwBuffer(uint8_t *refBuf, size_t size);

public:
    virtual ~HwBuffer();

    uint8_t *getData();

    size_t size();

private:
    uint8_t *buf = nullptr;
    size_t _size = 0;
    bool isRef = false;
};


#endif //HARDWAREVIDEOCODEC_HWBUFFER_H
