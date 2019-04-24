/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */

#include "../include/HwAbsFrame.h"

HwAbsFrame::HwAbsFrame(Type type) {
    this->type = type;
}

HwAbsFrame::~HwAbsFrame() {
    if (data) {
        delete[]data;
        data = nullptr;
    }
    dataSize = 0;
    format = 0;
}

uint8_t *HwAbsFrame::getData() { return data; }

uint64_t HwAbsFrame::getDataSize() { return dataSize; }

void HwAbsFrame::setData(uint8_t *data, uint64_t dataSize) {
    this->data = data;
    this->dataSize = dataSize;
}

void HwAbsFrame::setFormat(uint16_t format) { this->format = format; }

uint16_t HwAbsFrame::getFormat() { return format; }

HwAbsFrame::Type HwAbsFrame::getType() { return type; }

void HwAbsFrame::setPts(int64_t pts) { this->pts = pts; }

int64_t HwAbsFrame::getPts() { return pts; }

bool HwAbsFrame::isVideo() { return Type::VIDEO == type; }

bool HwAbsFrame::isAudio() { return Type::AUDIO == type; }