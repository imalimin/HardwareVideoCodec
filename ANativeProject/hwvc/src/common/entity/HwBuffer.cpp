/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/

#include "../include/HwBuffer.h"

HwBuffer *HwBuffer::alloc(size_t size) {
    return new HwBuffer(size);
}

HwBuffer *HwBuffer::wrap(uint8_t *buf, size_t size) {
    return new HwBuffer(buf, size);
}

HwBuffer::HwBuffer(size_t size) : Object() {
    this->_size = size;
    this->buf = new uint8_t[size];
    this->isRef = false;
}

HwBuffer::HwBuffer(uint8_t *refBuf, size_t size) : Object() {
    this->_size = size;
    this->buf = refBuf;
    this->isRef = true;
}

HwBuffer::~HwBuffer() {
    _size = 0;
    if (!isRef) {
        delete[] buf;
        buf = nullptr;
    }
}

size_t HwBuffer::size() { return this->_size; }

uint8_t *HwBuffer::getData() { return this->buf; }