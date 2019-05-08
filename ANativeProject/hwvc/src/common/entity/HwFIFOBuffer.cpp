/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/

#include "../include/HwFIFOBuffer.h"
#include "../include/HwMemFrameRef.h"
#include "../include/Logcat.h"
#include "../include/TimeUtils.h"

HwFIFOBuffer::HwFIFOBuffer(size_t capacity) : Object() {
    this->capacity = capacity;
    this->buf = new uint8_t[capacity];
    this->_size = 0;
    this->reader = first();
    this->writer = first();
}

HwFIFOBuffer::~HwFIFOBuffer() {
    if (buf) {
        delete[]buf;
        buf = nullptr;
        writer = nullptr;
        reader = nullptr;
    }
    capacity = 0;
}

void HwFIFOBuffer::push(uint8_t *data, size_t size) {
    if (end() - this->writer + 1 < size) {
        movePosition();
        while (end() - this->writer + 1 < size) {
            Logcat::e("HWVC", "Capacity is insufficient(left=%d). Wait", leftCapacity());
            notifyLock.wait();
            movePosition();
        }
    }
    memcpy(this->writer, data, size);
    writeReadLock.lock();
    this->writer += size;
    this->_size += size;
    writeReadLock.unlock();
    Logcat::e("HWVC", "HwFIFOBuffer::push(%d/%d/%d)", this->size(), leftCapacity(), capacity);
}

HwAbsFrame *HwFIFOBuffer::take(size_t size) {
    if (this->size() < size) {
        Logcat::e("HWVC", "HwFIFOBuffer::take b");
        return nullptr;
    }
    int64_t time = TimeUtils::getCurrentTimeUS();
    writeReadLock.lock();
    HwAbsFrame *frame = new HwMemFrameRef(this->reader, size);
    this->reader += size;
    this->_size -= size;
    writeReadLock.unlock();
    notifyLock.notify();
    Logcat::e("HWVC", "HwFIFOBuffer::take(%d/%d/%d)(%d, %d), cost: %lld", this->size(), leftCapacity(),
              capacity,
              frame->getData(), frame->getData() + 4,
              getCurrentTimeUS() - time);
    return frame;
}

bool HwFIFOBuffer::wantWrite(size_t size) {
    return false;
}

uint8_t *HwFIFOBuffer::first() {
    return this->buf;
}

uint8_t *HwFIFOBuffer::end() {
    return this->buf + capacity - 1;
}

size_t HwFIFOBuffer::leftCapacity() {
    if (this->writer > this->reader) {
        return capacity - (this->writer - this->reader);
    } else if (this->writer < this->reader) {
        return this->reader - this->writer;
    }
    return capacity - size();
}

bool HwFIFOBuffer::empty() {
    return 0 == leftCapacity();
}

size_t HwFIFOBuffer::size() {
    return _size;
}

void HwFIFOBuffer::movePosition() {
    writeReadLock.lock();
    size_t size = static_cast<size_t>(this->writer - reader);
    uint8_t tmp[size];
    memcpy(tmp, this->reader, size);
    memcpy(first(), tmp, size);
    this->reader = first();
    this->writer = first() + size;
    writeReadLock.unlock();
    Logcat::i("HWVC", "HwFIFOBuffer::movePosition(left=%d)", leftCapacity());
}