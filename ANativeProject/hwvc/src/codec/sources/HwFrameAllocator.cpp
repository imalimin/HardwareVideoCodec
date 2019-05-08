/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */

#include "../include/HwFrameAllocator.h"
#include "../include/HwVideoFrame.h"
#include "../include/HwAudioFrame.h"

HwFrameAllocator::HwFrameAllocator() {

}

HwFrameAllocator::~HwFrameAllocator() {
    unRefQueue.clear();
    refQueue.clear();
}

HwAbsMediaFrame *HwFrameAllocator::ref(AVFrame *avFrame) {
    if (!avFrame) return nullptr;
    if (avFrame->width * avFrame->height > 0) {
        return refVideo(avFrame);
    }
    return refAudio(avFrame);
}

void HwFrameAllocator::unRef(HwAbsMediaFrame **frame) {
    unRefQueue.push_front(frame[0]);
    refQueue.remove(frame[0]);
    frame[0] = nullptr;
}

HwAbsMediaFrame *HwFrameAllocator::refAudio(AVFrame *avFrame) {
    HwAbsMediaFrame *frame = nullptr;
    if (unRefQueue.size() > 0) {
        list<HwAbsMediaFrame *>::iterator itr = unRefQueue.begin();
        while (itr != unRefQueue.end()) {
            if (HwAbsMediaFrame::Type::AUDIO == (*itr)->getType()
                && (*itr)->getDataSize() == avFrame->linesize[0]) {//帧类型相同，data大小相等，则可以复用
                frame = *itr;
                unRefQueue.remove(frame);
            }
            ++itr;
        }
    }
    if (!frame) {
        frame = new HwAudioFrame(static_cast<uint16_t>(avFrame->channels),
                                 static_cast<uint32_t>(avFrame->sample_rate),
                                 static_cast<uint64_t>(avFrame->nb_samples));
        uint8_t *buffer = new uint8_t[avFrame->linesize[0]];
        frame->setData(buffer, static_cast<uint64_t>(avFrame->linesize[0]));
    }
    memset(frame->getData(), 0, frame->getDataSize());
    copyInfo(frame, avFrame);
    refQueue.push_front(frame);
    return frame;
}

HwAbsMediaFrame *HwFrameAllocator::refVideo(AVFrame *avFrame) {
    int size = avFrame->width * avFrame->height * 3 / 2;
    HwAbsMediaFrame *frame = nullptr;
    if (unRefQueue.size() > 0) {
        list<HwAbsMediaFrame *>::iterator itr = unRefQueue.begin();
        while (itr != unRefQueue.end()) {
            if (HwAbsMediaFrame::Type::VIDEO == (*itr)->getType()
                && (*itr)->getDataSize() == size) {//帧类型相同，data大小相等，则可以复用
                frame = *itr;
                unRefQueue.remove(frame);
            }
            ++itr;
        }
    }
    if (!frame) {
        frame = new HwVideoFrame(static_cast<uint32_t>(avFrame->width),
                                 static_cast<uint32_t>(avFrame->height));
        uint8_t *buffer = new uint8_t[size];
        frame->setData(buffer, static_cast<uint64_t>(size));
    }
    copyInfo(frame, avFrame);
    refQueue.push_front(frame);
    return frame;
}

void HwFrameAllocator::copyInfo(HwAbsMediaFrame *dest, AVFrame *src) {
    memcpy(dest->getData(), src->data[0], static_cast<size_t>(dest->getDataSize()));
    dest->setFormat(static_cast<uint16_t>(src->format));
    dest->setPts(src->pts);
}

HwAbsMediaFrame *HwFrameAllocator::ref(HwAbsMediaFrame *src) {
    HwAbsMediaFrame *frame = nullptr;
    if (unRefQueue.size() > 0) {
        list<HwAbsMediaFrame *>::iterator itr = unRefQueue.begin();
        while (itr != unRefQueue.end()) {
            if ((*itr)->getType() == src->getType()
                && (*itr)->getDataSize() == src->getDataSize()) {
                frame = *itr;
                unRefQueue.remove(frame);
            }
            ++itr;
        }
    }
    if (frame) {
        memcpy(frame->getData(), src->getData(), static_cast<size_t>(frame->getDataSize()));
        refQueue.push_front(frame);
    } else {
        if (src->isVideo()) {
            frame = static_cast<HwVideoFrame *>(src)->clone();
        } else if (src->isAudio()) {
            frame = static_cast<HwAudioFrame *>(src)->clone();
        }
    }
    return frame;
}