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

/**
 * 规则
 * 1. writer不能等于reader
 * 2. writer不能越过reader，同时reader也不能越过writer
 */
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
    Logcat::i("HWVC", "HwFIFOBuffer::push %d, %d",
              size,
              this->size());
    printBufferState();
    return size;
}

HwBuffer *HwFIFOBuffer::take(size_t size) {
    if (!buf || nullptr == this->reader) {
        Logcat::e("HWVC", "HwFIFOBuffer::take failed(unready)");
        return nullptr;
    }
    /*-----------------*/
    /* ... r ... w ... */
    /*-----------------*/
    if (this->reader < this->writer && this->reader + size >= this->writer) {
        Logcat::e("HWVC", "HwFIFOBuffer::take failed(cross a)");
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
//            if (left < size && left != 1) {
//                size_t right = size - left;
//                if (first() + right >= this->writer) {
//                    Logcat::e("HWVC", "HwFIFOBuffer::take failed(cross b)");
//                    return nullptr;
//                }
//                HwBuffer *buf = HwBuffer::alloc(size);
//                memcpy(buf->getData(), this->reader, left);
//                memcpy(buf->getData() + left, first(), right);
//                this->endFlag = end();//this->endFlag失效，一个循环只允许使用一次
//                this->reader = first() + right;
//                this->_size -= size;
//                notifyLock.notify();
//                Logcat::i("HWVC", "HwFIFOBuffer::take c %d, %d",
//                          buf->size(),
//                          this->size());
//                printBufferState();
//                return buf;
//            }
            HwBuffer *buf = HwBuffer::wrap(this->reader, left);
            this->endFlag = end();//this->endFlag失效，一个循环只允许使用一次
            /*-------------*/
            /* r ... w ... */
            /*-------------*/
            this->reader = first();
            this->_size -= left;
            notifyLock.notify();
            Logcat::i("HWVC", "HwFIFOBuffer::take a %d, %d",
                      buf->size(),
                      this->size());
            printBufferState();
            return buf;
        }
    }
    HwBuffer *buf = HwBuffer::wrap(this->reader, size);
    this->reader += size;
    this->_size -= size;
    notifyLock.notify();
    Logcat::i("HWVC", "HwFIFOBuffer::take b %d, %d",
              buf->size(),
              this->size());
    printBufferState();
    return buf;
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

size_t HwFIFOBuffer::size() {
    return _size;
}

void HwFIFOBuffer::flush() {
    this->reader = end();
    this->writer = first();
    this->_size = 0;
    notifyLock.notify();
    Logcat::i("HWVC", "HwFIFOBuffer::flush");
}

void HwFIFOBuffer::printBufferState() {
#if 1
    std::lock_guard<std::mutex> lock_guard(mutex);
    const uint8_t *reader = this->reader;
    const uint8_t *writer = this->writer;
    size_t delta = reader - writer;
    Logcat::i("HWVC HwFIFOBuffer",
              "/*--------------------------------------------------------------------------------------------------*/");
    if (delta > 0) {
        Logcat::i("HWVC HwFIFOBuffer", "/* %p .. %10d .. W%p .. %10d .. R%p .. %10d .. %p */",
                  first(),
                  writer - first(),
                  writer,
                  delta,
                  reader,
                  end() - reader,
                  end());
    } else if (delta < 0) {
        Logcat::i("HWVC HwFIFOBuffer", "/* %p .. %10d .. R%p .. %10d .. W%p .. %10d .. %p */",
                  first(),
                  reader - first(),
                  reader,
                  delta,
                  writer,
                  end() - writer,
                  end());
    } else {
        Logcat::i("HWVC HwFIFOBuffer", "/* %p .. %10d .. RW%p .. %10d .. %p */",
                  first(),
                  reader - first(),
                  reader,
                  end() - reader,
                  end());
    }
    Logcat::i("HWVC HwFIFOBuffer",
              "/*--------------------------------------------------------------------------------------------------*/");
#endif
}