//
// Created by mingyi.li on 2019/6/16.
//

#include "../include/HwSampleFormat.h"

HwSampleFormat::HwSampleFormat(HwFrameFormat format,
                               uint16_t channels,
                               uint32_t sampleRate)
        : Object() {
    this->format = format;
    this->channels = channels;
    this->sampleRate = sampleRate;
}

HwSampleFormat::~HwSampleFormat() {
    this->format = HW_FMT_NONE;
    this->channels = 0;
    this->sampleRate = 0;
}

HwFrameFormat HwSampleFormat::getFormat() { return format; }

uint16_t HwSampleFormat::getChannels() { return channels; }

uint32_t HwSampleFormat::getSampleRate() { return sampleRate; }