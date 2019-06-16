/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */

#include "../include/HwAbsMediaFrame.h"

HwAbsMediaFrame::HwAbsMediaFrame(HwSourcesAllocator *allocator, Type type) : HwSources(allocator),
                                                                             HwAbsFrame() {
    this->type = type;
}

HwAbsMediaFrame::~HwAbsMediaFrame() {
    format = 0;
}

void HwAbsMediaFrame::setFormat(uint16_t format) { this->format = format; }

uint16_t HwAbsMediaFrame::getFormat() { return format; }

HwAbsMediaFrame::Type HwAbsMediaFrame::getType() { return type; }

void HwAbsMediaFrame::setPts(int64_t pts) { this->pts = pts; }

int64_t HwAbsMediaFrame::getPts() { return pts; }

bool HwAbsMediaFrame::isVideo() { return Type::VIDEO == type; }

bool HwAbsMediaFrame::isAudio() { return Type::AUDIO == type; }