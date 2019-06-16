/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */

#include "../include/HwFrameAllocator.h"
#include "../include/HwVideoFrame.h"
#include "../include/HwAudioFrame.h"

HwFrameAllocator::HwFrameAllocator() : HwSourcesAllocator() {

}

HwFrameAllocator::~HwFrameAllocator() {
    unRefLock.lock();
    list<HwAbsMediaFrame *>::iterator itr = unRefQueue.begin();
    while (itr != unRefQueue.end()) {
        HwAbsMediaFrame *frame = *itr;
        delete frame;
        ++itr;
    }
    unRefQueue.clear();
    unRefLock.unlock();
    refLock.lock();
    itr = refQueue.begin();
    while (itr != refQueue.end()) {
        HwAbsMediaFrame *frame = *itr;
        delete frame;
        ++itr;
    }
    refQueue.clear();
    refLock.unlock();
}

HwAbsMediaFrame *HwFrameAllocator::ref(AVFrame *avFrame) {
    if (!avFrame) return nullptr;
    if (avFrame->width * avFrame->height > 0) {
        return refVideo(avFrame);
    }
    return refAudio(avFrame);
}

bool HwFrameAllocator::recycle(HwSources **entity) {
    HwAbsMediaFrame *frame = reinterpret_cast<HwAbsMediaFrame *>(entity[0]);
    entity[0] = nullptr;
    if (!isRef(frame)) {
        return false;
    }
    unRefLock.lock();
    unRefQueue.push_front(frame);
    unRefLock.unlock();
    refLock.lock();
    refQueue.remove(frame);
    refLock.unlock();
    return true;
}

bool HwFrameAllocator::isRef(HwAbsMediaFrame *frame) {
    list<HwAbsMediaFrame *>::iterator itr = refQueue.begin();
    while (itr != refQueue.end()) {
        if (*itr == frame) {
            return true;
        }
        ++itr;
    }
    return false;
}

HwAbsMediaFrame *HwFrameAllocator::refAudio(AVFrame *avFrame) {
    HwAbsMediaFrame *frame = nullptr;
    unRefLock.lock();
    if (unRefQueue.size() > 0) {
        list<HwAbsMediaFrame *>::iterator itr = unRefQueue.begin();
        while (itr != unRefQueue.end()) {
            if ((*itr)->isAudio()
                && (*itr)->getBufferSize() == avFrame->linesize[0]) {//帧类型相同，data大小相等，则可以复用
                frame = *itr;
                unRefQueue.remove(frame);
            }
            ++itr;
        }
    }
    unRefLock.unlock();
    if (!frame) {
        frame = new HwAudioFrame(this,
                                 HwAbsMediaFrame::convertToAudioFrameFormat(
                                         static_cast<AVSampleFormat>(avFrame->format)),
                                 static_cast<uint16_t>(avFrame->channels),
                                 static_cast<uint32_t>(avFrame->sample_rate),
                                 static_cast<uint64_t>(avFrame->nb_samples));
    }
    memset(frame->getBuffer()->getData(), 0, frame->getBufferSize());
    copyInfo(frame, avFrame);
    refLock.lock();
    refQueue.push_front(frame);
    refLock.unlock();
    return frame;
}

HwAbsMediaFrame *HwFrameAllocator::refVideo(AVFrame *avFrame) {
    int size = avFrame->width * avFrame->height * 3 / 2;
    HwAbsMediaFrame *frame = nullptr;
    unRefLock.lock();
    if (unRefQueue.size() > 0) {
        list<HwAbsMediaFrame *>::iterator itr = unRefQueue.begin();
        while (itr != unRefQueue.end()) {
            if ((*itr)->isVideo()
                && (*itr)->getBufferSize() == size) {//帧类型相同，data大小相等，则可以复用
                frame = *itr;
                unRefQueue.remove(frame);
            }
            ++itr;
        }
    }
    unRefLock.unlock();
    if (!frame) {
        frame = new HwVideoFrame(this, HW_IMAGE_YV12,
                                 static_cast<uint32_t>(avFrame->width),
                                 static_cast<uint32_t>(avFrame->height));
    }
    copyInfo(frame, avFrame);
    refLock.lock();
    refQueue.push_front(frame);
    refLock.unlock();
    return frame;
}

void HwFrameAllocator::copyInfo(HwAbsMediaFrame *dest, AVFrame *src) {
    memcpy(dest->getBuffer()->getData(), src->data[0], dest->getBufferSize());
    dest->setPts(src->pts);
}

HwAbsMediaFrame *HwFrameAllocator::ref(HwAbsMediaFrame *src) {
    HwAbsMediaFrame *frame = nullptr;
    unRefLock.lock();
    if (unRefQueue.size() > 0) {
        list<HwAbsMediaFrame *>::iterator itr = unRefQueue.begin();
        while (itr != unRefQueue.end()) {
            if ((*itr)->getFormat() == src->getFormat()
                && (*itr)->getBufferSize() == src->getBufferSize()) {
                frame = *itr;
                unRefQueue.remove(frame);
            }
            ++itr;
        }
    }
    unRefLock.unlock();
    if (frame) {
        memcpy(frame->getBuffer()->getData(), src->getBuffer()->getData(), frame->getBufferSize());
    } else {
        if (src->isVideo()) {
            frame = static_cast<HwVideoFrame *>(src)->clone();
        } else if (src->isAudio()) {
            HwAudioFrame *audioFrame = static_cast<HwAudioFrame *>(src);

            frame = new HwAudioFrame(this, audioFrame->getFormat(), audioFrame->getChannels(),
                                     audioFrame->getSampleRate(), audioFrame->getSampleCount());
            audioFrame->clone(frame);
        }
    }
    refLock.lock();
    refQueue.push_front(frame);
    refLock.unlock();
    return frame;
}

void HwFrameAllocator::printInfo() {
    Logcat::i("HWVC", "HwFrameAllocator(%p)::info: ref=%d, unRef=%d",
              this,
              refQueue.size(),
              unRefQueue.size());
}