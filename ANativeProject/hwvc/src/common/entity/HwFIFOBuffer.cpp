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
    this->reader = nullptr;
    this->writer = first();
    this->endFlag = end();
}

HwFIFOBuffer::~HwFIFOBuffer() {
    flush();
    if (buf) {
        delete[]buf;
        buf = nullptr;
        writer = nullptr;
        reader = nullptr;
        endFlag = end();
    }
    capacity = 0;
}

void HwFIFOBuffer::push(uint8_t *data, size_t size) {
    if (!buf) {
        return;
    }
    if (this->writer + size > end() + 1) {
        if (first() + size <= this->reader) {//保证不会越过reader
            this->endFlag = this->writer - 1;
            if (this->endFlag < first()) {
                this->endFlag = first();
            }
            this->writer = first();
        } else {
            if (isLogEnable()) {
                Logcat::e("HWVC", "HwFIFOBuffer::push Capacity is insufficient(left=%d). Wait",
                          leftCapacity());
            }
            return;
        }
    }
    if (this->writer + size > this->reader) {//保证不会越过reader
        if (isLogEnable()) {
            Logcat::e("HWVC", "HwFIFOBuffer::push Capacity is insufficient(left=%d). Wait",
                      leftCapacity());
        }
        return;
    }
    memcpy(this->writer, data, size);
    this->writer += size;
    this->_size += size;
    if (nullptr == this->reader) {
        this->reader = first();
    }
    if (isLogEnable()) {
        Logcat::i("HWVC", "HwFIFOBuffer::push(%d/%d/%d)", this->size(), leftCapacity(), capacity);
    }
}

HwAbsFrame *HwFIFOBuffer::take(size_t size) {
    if (!buf) {
        return nullptr;
    }
    if (this->endFlag - this->reader + 1 <= size) {
        HwAbsFrame *frame = new HwMemFrameRef(this->reader, this->endFlag - this->reader + 1);
        this->reader = first();
        this->_size -= size;
        return frame;
    }

    if (nullptr == this->reader || this->reader + size > this->writer) {//保证不会越过reader
        if (isLogEnable()) {
            Logcat::e("HWVC", "HwFIFOBuffer::take failed(size=%d, want=%d)", this->size(), size);
        }
        return nullptr;
    }
    int64_t time = TimeUtils::getCurrentTimeUS();
//    writeReadLock.lock();
    HwAbsFrame *frame = new HwMemFrameRef(this->reader, size);
    this->reader += size;
    this->_size -= size;
//    writeReadLock.unlock();
    if (isLogEnable()) {
        Logcat::i("HWVC", "HwFIFOBuffer::take(%d/%d/%d)(%d, %d), cost: %lld", this->size(),
                  leftCapacity(),
                  capacity,
                  frame->getData(), frame->getData() + 4,
                  getCurrentTimeUS() - time);
    }
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
    size_t size = static_cast<size_t>(this->writer - this->reader);
    if (0 == size) {
        return;
    }
    memcpy(first(), this->reader, size);
    this->reader = first();
    this->writer = first() + size;
    if (isLogEnable()) {
        Logcat::i("HWVC", "HwFIFOBuffer::movePosition(left=%d)", leftCapacity());
    }
}

void HwFIFOBuffer::flush() {
    this->reader = first();
    this->writer = first();
    this->_size = 0;
    if (isLogEnable()) {
        Logcat::i("HWVC", "HwFIFOBuffer::push(%d/%d/%d)", this->size(), leftCapacity(), capacity);
    }
}