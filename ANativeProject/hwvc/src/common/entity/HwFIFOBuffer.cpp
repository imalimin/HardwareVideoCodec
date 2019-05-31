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
    this->reader = end();
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
        endFlag = nullptr;
    }
    capacity = 0;
}

bool HwFIFOBuffer::willCross(uint8_t *flag, uint8_t *pointer, size_t size) {
    if (pointer < flag) {
        if (pointer + size >= flag) {//TODO pointer == flag有歧义，所以也认为是cross，这会导致buf内总有一部分数据无法读取
            return true;
        }
    }
    return false;
}

bool HwFIFOBuffer::willWrite(size_t size) {
    /*-----------------*/
    /* ... w ... r ... */
    /*-----------------*/
    if (this->writer < this->reader && this->writer + size >= this->reader) {
        return false;
    }
    /*-----------------*/
    /* ... r ... w ... */
    /*-----------------*/
    if (this->writer > this->reader) {
        size_t left = end() + 1 - this->writer;
        if (left <= size) {//末尾剩余空间不足，需要移动到first
            /*-----------------*/
            /* r ... w ... */
            /*-----------------*/
            if (this->reader == first()) {
                return false;
            }
            if (left < size) {
                this->writer = first();
                /*-------------*/
                /* w ... r ... */
                /*-------------*/
                if (this->writer + size >= this->reader) {
                    return false;
                }
                this->endFlag = this->writer - 1;
            }
        }
    }
    return true;
}

size_t HwFIFOBuffer::willRead(size_t size) {
    size_t left = this->endFlag + 1 - this->reader;
    if (left <= size) {
        return left;
    }
    return 0;
}

uint8_t *HwFIFOBuffer::move(uint8_t *pointer, size_t size) {
    if (pointer + size > end()) {
        return first();
    }
    return pointer + size;
}

size_t HwFIFOBuffer::push(uint8_t *data, size_t size) {
    if (!buf) {
        return 0;
    }
    while (!willWrite(size)) {
        Logcat::e("HWVC", "HwFIFOBuffer::push Capacity is insufficient(left=%d). Wait",
                  leftCapacity());
        notifyLock.wait();
    }
    memcpy(this->writer, data, size);
    this->writer = move(this->writer, size);
    this->_size += size;
    Logcat::i("HWVC", "HwFIFOBuffer::push(%d/%d/%d)(%p, %p)", this->size(),
              leftCapacity(),
              capacity,
              this->writer,
              this->reader);
    return size;
}

HwAbsFrame *HwFIFOBuffer::take(size_t size) {
    if (!buf || nullptr == this->reader) {
        Logcat::e("HWVC", "HwFIFOBuffer::take failed(unready)");
        return nullptr;
    }
    /*-----------------*/
    /* ... r ... w ... */
    /*-----------------*/
    if (this->reader < this->writer && this->reader + size >= this->writer) {
        Logcat::e("HWVC", "HwFIFOBuffer::take failed(cross)");
        return nullptr;
    }
    /*-----------------*/
    /* ... w ... r ... */
    /*-----------------*/
    if (this->reader > this->writer) {
        size_t left = this->endFlag + 1 - this->reader;
        if (left <= size) {
            /*-------------*/
            /* w ... r ... */
            /*-------------*/
            if (this->writer == first()) {
                Logcat::e("HWVC", "HwFIFOBuffer::take failed(First occupied)");
                return nullptr;
            }
            if (left <= 0) {
                Logcat::e("HWVC", "HwFIFOBuffer::take failed(error) %d", left);
                return nullptr;
            }
            HwAbsFrame *frame = new HwMemFrameRef(this->reader, left);
            this->endFlag = end();//this->endFlag失效，一个循环只允许使用一次
            /*-------------*/
            /* r ... w ... */
            /*-------------*/
            this->reader = first();
            this->_size -= left;
            notifyLock.notify();
            Logcat::i("HWVC", "HwFIFOBuffer::take a(%d/%d/%d, %lld)(%p, %p)", this->size(),
                      leftCapacity(),
                      capacity,
                      frame->getDataSize(),
                      this->writer,
                      this->reader);
            return frame;
        }
    }
    HwAbsFrame *frame = new HwMemFrameRef(this->reader, size);
    this->reader += size;
    this->_size -= size;
    notifyLock.notify();
    Logcat::i("HWVC", "HwFIFOBuffer::take b(%d/%d/%d, %lld)(%p, %p)", this->size(),
              leftCapacity(),
              capacity,
              frame->getDataSize(),
              this->writer,
              this->reader);
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
    Logcat::i("HWVC", "HwFIFOBuffer::movePosition(left=%d)", leftCapacity());
}

void HwFIFOBuffer::flush() {
    this->reader = end();
    this->writer = first();
    this->_size = 0;
    notifyLock.notify();
    Logcat::i("HWVC", "HwFIFOBuffer::flush");
}