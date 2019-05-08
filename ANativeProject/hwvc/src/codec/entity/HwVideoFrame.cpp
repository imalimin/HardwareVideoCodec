/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */

#include "../include/HwVideoFrame.h"

HwVideoFrame::HwVideoFrame(uint32_t width, uint32_t height) : HwAbsMediaFrame(Type::VIDEO) {
    this->width = width;
    this->height = height;
}

HwVideoFrame::~HwVideoFrame() {
    this->width = 0;
    this->height = 0;
}

void HwVideoFrame::setWidth(uint32_t width) { this->width = width; }

void HwVideoFrame::setHeight(uint32_t height) { this->height = height; }

uint32_t HwVideoFrame::getWidth() { return width; }

uint32_t HwVideoFrame::getHeight() { return height; }

HwAbsMediaFrame *HwVideoFrame::clone() {
    if (!isVideo()) {
        return nullptr;
    }
    HwVideoFrame *destFrame = new HwVideoFrame(width, height);
    destFrame->setPts(getPts());
    destFrame->setFormat(getFormat());
    uint8_t *buffer = new uint8_t[getDataSize()];
    destFrame->setData(buffer, getDataSize());
    memcpy(destFrame->getData(), getData(), static_cast<size_t>(destFrame->getDataSize()));
    return destFrame;
}