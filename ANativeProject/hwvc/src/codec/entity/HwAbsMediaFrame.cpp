/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */

#include "../include/HwAbsMediaFrame.h"

AVSampleFormat HwAbsMediaFrame::convertAudioFrameFormat(HwFrameFormat format) {
    if (format >= HW_SAMPLE_U8 && format <= HW_SAMPLE_DBL) {
        return static_cast<AVSampleFormat>(format - HW_SAMPLE_U8);
    }
    return AV_SAMPLE_FMT_NONE;
}

HwFrameFormat HwAbsMediaFrame::convertToAudioFrameFormat(AVSampleFormat format) {
    if (format >= AV_SAMPLE_FMT_U8 && format <= AV_SAMPLE_FMT_DBL) {
        return static_cast<HwFrameFormat>(format + HW_SAMPLE_U8);
    }
    return HW_FMT_NONE;
}

int HwAbsMediaFrame::getBytesPerSample(HwFrameFormat format) {
    return av_get_bytes_per_sample(convertAudioFrameFormat(format));
}

int HwAbsMediaFrame::getImageSize(HwFrameFormat format, int width, int height) {
    switch (format) {
        case HW_IMAGE_RGB:
            return width * height * 3;
        case HW_IMAGE_RGBA:
            return width * height * 4;
        case HW_IMAGE_YV12:
        case HW_IMAGE_NV12:
            return width * height * 3 / 2;
        default:
            return 0;
    }
}

HwAbsMediaFrame::HwAbsMediaFrame(HwSourcesAllocator *allocator,
                                 HwFrameFormat format,
                                 size_t size) : HwSources(allocator),
                                                HwAbsFrame(size) {
    this->format = format;
}

HwAbsMediaFrame::~HwAbsMediaFrame() {
}

void HwAbsMediaFrame::setFormat(HwFrameFormat format) { this->format = format; }

HwFrameFormat HwAbsMediaFrame::getFormat() { return format; }

void HwAbsMediaFrame::setPts(int64_t pts) { this->pts = pts; }

int64_t HwAbsMediaFrame::getPts() { return pts; }

bool HwAbsMediaFrame::isVideo() {
    return getFormat() >= HW_IMAGE_RGB && getFormat() < HW_SAMPLE_U8;
}

bool HwAbsMediaFrame::isAudio() {
    return getFormat() >= HW_SAMPLE_U8 && getFormat() <= HW_SAMPLE_DBL;
}