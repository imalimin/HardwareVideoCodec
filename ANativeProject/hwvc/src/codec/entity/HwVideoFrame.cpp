/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */

#include "../include/HwVideoFrame.h"
#include "Logcat.h"

HwVideoFrame::HwVideoFrame(uint32_t width, uint32_t height) : HwAbsMediaFrame(Type::VIDEO) {
    this->width = width;
    this->height = height;
}

HwVideoFrame::~HwVideoFrame() {
    setSize(0, 0);
}

void HwVideoFrame::setSize(uint32_t width, uint32_t height) {
    this->width = width;
    this->height = height;
}

uint32_t HwVideoFrame::getWidth() { return width; }

uint32_t HwVideoFrame::getHeight() { return height; }

uint64_t HwVideoFrame::duration() {
    return 0;
}

HwAbsMediaFrame *HwVideoFrame::clone() {
    HwVideoFrame *destFrame = new HwVideoFrame(width, height);
    destFrame->setPts(getPts());
    destFrame->setFormat(getFormat());
    uint8_t *buffer = new uint8_t[getDataSize()];
    destFrame->setData(buffer, getDataSize());
    memcpy(destFrame->getData(), getData(), static_cast<size_t>(destFrame->getDataSize()));
    return destFrame;
}

void HwVideoFrame::clone(HwAbsMediaFrame *src) {
    if (!src || !src->isVideo() || src->getDataSize() < getDataSize()) {
        Logcat::e("HWVC", "Invalid video frame");
        return;
    }
    HwVideoFrame *srcFrame = dynamic_cast<HwVideoFrame *>(src);
    srcFrame->setPts(getPts());
    srcFrame->setFormat(getFormat());
    srcFrame->setSize(getWidth(), getHeight());
    memcpy(srcFrame->getData(), getData(), getDataSize());
    srcFrame->setData(srcFrame->getData(), getDataSize());
}