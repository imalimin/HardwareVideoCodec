/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/

#ifndef HARDWAREVIDEOCODEC_HWMEMFIFO_H
#define HARDWAREVIDEOCODEC_HWMEMFIFO_H

#include "Object.h"
#include "HwBuffer.h"
#include "SimpleLock.h"
#include <mutex>

class HwFIFOBuffer : public Object {
public:
    HwFIFOBuffer(size_t capacity);

    HwFIFOBuffer(size_t capacity, bool writeMode);

    virtual ~HwFIFOBuffer();

    /**
     * 把数据写入fifo
     * @data 数据指针
     * @size data的大小
     * @return 返回成功push的size，0表示失败
     */
    size_t push(uint8_t *data, size_t size);

    /**
     * 从fifo读取数据
     * @size 期望得到的size
     * @return 返回数据片段映射，大小小于或等于size，该内存片段由fifo维护，切勿进行写操作
     */
    HwBuffer *take(size_t size);

    size_t size();

    void flush();

private:
    uint8_t *first();

    uint8_t *end();

    size_t leftCapacity();

    bool willWrite(size_t size);

    bool willRead(size_t size);

    uint8_t *move(uint8_t *pointer, size_t size);

    void printBufferState();

private:
    /**
     * true: push阻塞
     * false: take阻塞
     */
    bool writeMode = true;
    uint8_t *buf = nullptr;
    size_t capacity = 0;
    size_t _size = 0;
    uint8_t *reader = nullptr;
    uint8_t *writer = nullptr;
    uint8_t *endFlag = nullptr;

    SimpleLock notifyLock;
    std::mutex mutex;
    bool requestFlush = false;
};


#endif //HARDWAREVIDEOCODEC_HWMEMFIFO_H
