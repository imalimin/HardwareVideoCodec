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
    if (decoder) {
        delete decoder;
        decoder = nullptr;
    }
    if (hwFrameAllocator) {
        delete hwFrameAllocator;
        hwFrameAllocator = nullptr;
    }
    if (file) {
        fclose(file);
        file = nullptr;
    }
}

bool AsynAudioDecoder::prepare(string path) {
    file = fopen("/sdcard/2.pcm", "wb");
    playState = PAUSE;
    if (decoder) {
        if (!decoder->prepare(path)) {
            return false;
        }
    }
    if (!pipeline) {
        pipeline = new EventPipeline("AsynAudioDecoder");
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
    grabLock.notify();
    if (STOP != playState) {
        playState = STOP;
    }
}

void AsynAudioDecoder::loop() {
    if (PLAYING != playState)
        return;
    pipeline->queueEvent([this] {
        if (!grab()) {
            Logcat::i("HWVC", "AsynAudioDecoder stop loop");
            return;
        }
        loop();
    });
}

bool AsynAudioDecoder::grab() {
    if (cache.size() >= 10) {
//        grabLock.wait();
//        Logcat::i("HWVC", "AsynAudioDecoder::grab wait: %d", cache.size());
        return true;
    }
//    Logcat::i("HWVC", "HwFrameAllocator::info: cache %d", cache.size());
    HwAbsFrame *frame = nullptr;
    int ret = decoder->grab(&frame);
    frame = hwFrameAllocator->ref(frame);
    if (frame) {
        cache.push(frame);
    }
    return MEDIA_TYPE_EOF != ret;
}

int AsynAudioDecoder::grab(HwAbsFrame **frame) {
    if (STOP == playState || cache.empty()) {
        return MEDIA_TYPE_UNKNOWN;
    }
    if (outputFrame) {
        hwFrameAllocator->unRef(&outputFrame);
    }
    hwFrameAllocator->printInfo();
    outputFrame = cache.back();
    cache.pop();
    grabLock.notify();
    *frame = outputFrame;
    if (file) {
        fwrite(outputFrame->getData(), 1, outputFrame->getDataSize(), file);
    }
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