/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */

#include "../include/HwAudioFrame.h"

HwAudioFrame::HwAudioFrame(uint16_t channels, uint32_t sampleRate, uint64_t sampleCount)
        : HwAbsMediaFrame(Type::AUDIO) {
    this->channels = channels;
    this->sampleRate = sampleRate;
    this->sampleCount = sampleCount;
}

HwAudioFrame::~HwAudioFrame() {
    channels = 0;
    sampleRate = 0;
    sampleCount = 0;
}

uint16_t HwAudioFrame::getChannels() { return channels; }

uint32_t HwAudioFrame::getSampleRate() { return sampleRate; }

uint64_t HwAudioFrame::getSampleCount() { return sampleCount; }

HwAbsMediaFrame *HwAudioFrame::clone() {
    if (!isAudio()) {
        return nullptr;
    }
    HwAudioFrame *destFrame = new HwAudioFrame(channels, sampleRate, sampleCount);
    destFrame->setPts(getPts());
    destFrame->setFormat(getFormat());
    uint8_t *buffer = new uint8_t[getDataSize()];
    destFrame->setData(buffer, getDataSize());
    memcpy(destFrame->getData(), getData(), static_cast<size_t>(destFrame->getDataSize()));
    return destFrame;
}