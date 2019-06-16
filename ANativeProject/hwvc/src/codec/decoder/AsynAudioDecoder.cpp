/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/

#include "../include/AsynAudioDecoder.h"
#include "TimeUtils.h"

AsynAudioDecoder::AsynAudioDecoder() : AbsAudioDecoder() {
    hwFrameAllocator = new HwFrameAllocator();
    decoder = new DefaultAudioDecoder();
}

AsynAudioDecoder::~AsynAudioDecoder() {
    stop();
    if (pipeline) {
        delete pipeline;
        pipeline = nullptr;
    }
    releaseLock.lock();
    if (decoder) {
        delete decoder;
        decoder = nullptr;
    }
    if (hwFrameAllocator) {
        delete hwFrameAllocator;
        hwFrameAllocator = nullptr;
    }
    releaseLock.unlock();
}

bool AsynAudioDecoder::prepare(string path) {
    playState = PAUSE;
    if (!pipeline) {
        pipeline = new EventPipeline("AsynAudioDecoder");
    }
    if (decoder) {
        if (!decoder->prepare(path)) {
            Logcat::e("HWVC", "AsynAudioDecoder::prepare failed");
            return false;
        }
    }
//    start();
    return true;
}

void AsynAudioDecoder::seek(int64_t us) {

}

void AsynAudioDecoder::start() {
    if (STOP == playState || PLAYING == playState) {
        return;
    }
    playState = PLAYING;
    loop();
}

void AsynAudioDecoder::pause() {
    if (STOP != playState) {
        playState = PAUSE;
    }
}

void AsynAudioDecoder::stop() {
    if (STOP != playState) {
        playState = STOP;
    }
    grabLock.notify();
}

void AsynAudioDecoder::loop() {
    if (PLAYING != playState || !pipeline) {
        Logcat::i("HWVC", "AsynAudioDecoder::loop skip loop");
        return;
    }
    pipeline->queueEvent([this] {
        if (!grab()) {
            pause();
            Logcat::i("HWVC", "AsynAudioDecoder::loop EOF");
            return;
        }
        loop();
    });
}

bool AsynAudioDecoder::grab() {
    if (cache.size() >= 10) {
        grabLock.wait();
        return true;
    }
//    Logcat::i("HWVC", "HwFrameAllocator::info: cache %d", cache.size());
    HwAbsMediaFrame *frame = nullptr;
    int ret = MEDIA_TYPE_UNKNOWN;
    releaseLock.lock();
    if (decoder) {
        ret = decoder->grab(&frame);
    }
    if (hwFrameAllocator && frame) {
        frame = hwFrameAllocator->ref(frame);
        cache.push(frame);
    }
    releaseLock.unlock();
    return MEDIA_TYPE_EOF != ret;
}

int AsynAudioDecoder::grab(HwAbsMediaFrame **frame) {
    if (STOP == playState || cache.empty()) {
        return MEDIA_TYPE_UNKNOWN;
    }
    if (outputFrame) {
        outputFrame->recycle();
    }
    hwFrameAllocator->printInfo();
    outputFrame = cache.back();
    cache.pop();
    grabLock.notify();
    *frame = outputFrame;
//    memset(outputFrame->getData(), 1, outputFrame->getDataSize());
    if ((*frame)->isAudio()) {
        return MEDIA_TYPE_AUDIO;
    }
    return MEDIA_TYPE_VIDEO;
}

int AsynAudioDecoder::getChannels() {
    if (decoder) {
        return decoder->getChannels();
    }
    return 0;
}

int AsynAudioDecoder::getSampleHz() {
    if (decoder) {
        return decoder->getSampleHz();
    }
    return 0;
}

int AsynAudioDecoder::getSampleFormat() {
    if (decoder) {
        return decoder->getSampleFormat();
    }
    return AV_SAMPLE_FMT_NONE;
}

int AsynAudioDecoder::getPerSampleSize() {
    if (decoder) {
        return decoder->getPerSampleSize();
    }
    return 0;
}

int64_t AsynAudioDecoder::getAudioDuration() {
    if (decoder) {
        decoder->getAudioDuration();
    }
    return 0;
}