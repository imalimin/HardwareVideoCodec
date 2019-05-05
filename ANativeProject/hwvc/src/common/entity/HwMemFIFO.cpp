//
// Created by limin on 2019/5/6.
//

#include "../include/HwMemFIFO.h"
#include "../include/Logcat.h"

HwMemFIFO::HwMemFIFO(size_t capacity) : Object() {
    this->capacity = capacity;
    this->buf = new uint8_t[capacity];
    this->_size = 0;
    this->reader = nullptr;
    this->writer = first();
}

HwMemFIFO::~HwMemFIFO() {
    if (buf) {
        delete[]buf;
        buf = nullptr;
        writer = nullptr;
        reader = nullptr;
    }
    capacity = 0;
}

void HwMemFIFO::push(uint8_t *data, size_t size) {
    writeReadLock.lock();
    if (empty()) {
        Logcat::e("HWVC", "Capacity is insufficient, left=%d", leftCapacity());
        return;
    }
    if (this->writer > this->reader) {
        int right = static_cast<int>(end() - this->writer + 1);
        memcpy(this->writer, data, right);
        memcpy(first(), data + right, size - right);
        this->writer = first() + size - right;
    } else if (this->writer < this->reader) {
        memcpy(this->writer, data, size);
        this->writer += size;
    }
    this->_size += size;
    writeReadLock.unlock();
}

HwMemFrame *HwMemFIFO::take(size_t size) {
    writeReadLock.lock();
    if (this->size() < size) {
        return nullptr;
    }
    HwMemFrame *frame = new HwMemFrame();
    frame->setData(new uint8_t[size], size);
    if (this->writer > this->reader) {
        memcpy(frame->getData(), this->reader, size);
        this->reader += size;
    } else if (this->writer < this->reader) {
        int right = static_cast<int>(end() - this->reader + 1);
        memcpy(this->reader, frame->getData(), right);
        memcpy(first(), frame->getData() + right, size - right);
        this->reader = first() + size - right;
    }
    this->_size -= size;
    writeReadLock.unlock();
    return frame;
}

bool HwMemFIFO::wantWrite(size_t size) {
    return false;
}

uint8_t *HwMemFIFO::first() {
    return this->buf;
}

uint8_t *HwMemFIFO::end() {
    return this->buf + capacity - 1;
}

size_t HwMemFIFO::leftCapacity() {
    if (this->writer > this->reader) {
        return capacity - (this->writer - this->reader);
    } else if (this->writer < this->reader) {
        return this->reader - this->writer;
    }
    return 0;
}

bool HwMemFIFO::empty() {
    return 0 == leftCapacity();
}

size_t HwMemFIFO::size() {
    return _size;
}