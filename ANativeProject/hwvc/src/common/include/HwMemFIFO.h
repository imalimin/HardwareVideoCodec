//
// Created by limin on 2019/5/6.
//

#ifndef HARDWAREVIDEOCODEC_HWMEMFIFO_H
#define HARDWAREVIDEOCODEC_HWMEMFIFO_H

#include "Object.h"
#include "HwMemFrame.h"
#include "SimpleLock.h"

class HwMemFIFO : public Object {
public:
    HwMemFIFO(size_t capacity);

    virtual ~HwMemFIFO();

    void push(uint8_t *data, size_t size);

    HwMemFrame *take(size_t size);

    size_t size();

private:
    bool wantWrite(size_t size);

    uint8_t *first();

    uint8_t *end();

    size_t leftCapacity();

    bool empty();

private:
    uint8_t *buf = nullptr;
    size_t capacity = 0;
    size_t _size = 0;
    uint8_t *reader = nullptr;
    uint8_t *writer = nullptr;
    SimpleLock writeReadLock;
};


#endif //HARDWAREVIDEOCODEC_HWMEMFIFO_H
