//
// Created by limin on 2019/5/6.
//

#include "../include/HwMemFrame.h"

HwMemFrame::HwMemFrame() : Object() {

}

HwMemFrame::~HwMemFrame() {
    if (data) {
        delete[]data;
        data = nullptr;
    }
    dataSize = 0;
}

uint8_t *HwMemFrame::getData() { return data; }

uint64_t HwMemFrame::getDataSize() { return dataSize; }

void HwMemFrame::setData(uint8_t *data, uint64_t dataSize) {
    this->data = data;
    this->dataSize = dataSize;
}