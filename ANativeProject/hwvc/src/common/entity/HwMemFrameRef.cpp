/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/

#include "../include/HwMemFrameRef.h"

HwMemFrameRef::HwMemFrameRef(uint8_t *ref, uint64_t size) : HwAbsFrame() {
    setData(ref, size);
}

HwMemFrameRef::~HwMemFrameRef() {
    this->ref = nullptr;
}

uint8_t *HwMemFrameRef::getData() {
    return ref;
}

void HwMemFrameRef::setData(uint8_t *data, uint64_t dataSize) {
    HwAbsFrame::setData(nullptr, dataSize);
    this->ref = data;
}