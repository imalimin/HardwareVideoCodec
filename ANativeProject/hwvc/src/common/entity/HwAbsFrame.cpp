/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/

#include "../include/HwAbsFrame.h"

HwAbsFrame::HwAbsFrame() : Object() {

}

HwAbsFrame::~HwAbsFrame() {
    if (data) {
        delete[]data;
        data = nullptr;
    }
    dataSize = 0;
}

uint8_t *HwAbsFrame::getData() { return data; }

uint64_t HwAbsFrame::getDataSize() { return dataSize; }

void HwAbsFrame::setData(uint8_t *data, uint64_t dataSize) {
    this->data = data;
    this->dataSize = dataSize;
}