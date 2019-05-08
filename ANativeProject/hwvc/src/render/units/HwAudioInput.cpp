/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/

#include "../include/HwAudioInput.h"
#include "TimeUtils.h"

HwAudioInput::HwAudioInput() : HwStreamMedia() {
    name = __FUNCTION__;
    this->lock = new SimpleLock();
    registerEvent(EVENT_COMMON_PREPARE, reinterpret_cast<EventFunc>(&HwAudioInput::eventPrepare));
    registerEvent(EVENT_AUDIO_START, reinterpret_cast<EventFunc>(&HwAudioInput::eventStart));
    registerEvent(EVENT_AUDIO_PAUSE, reinterpret_cast<EventFunc>(&HwAudioInput::eventPause));
    registerEvent(EVENT_AUDIO_STOP, reinterpret_cast<EventFunc>(&HwAudioInput::eventStop));
    registerEvent(EVENT_AUDIO_SEEK, reinterpret_cast<EventFunc>(&HwAudioInput::eventSeek));
    registerEvent(EVENT_AUDIO_LOOP, reinterpret_cast<EventFunc>(&HwAudioInput::eventLoop));
    registerEvent(EVENT_AUDIO_SET_SOURCE,
                  reinterpret_cast<EventFunc>(&HwAudioInput::eventSetSource));
    decoder = new AsynAudioDecoder();
}

HwAudioInput::HwAudioInput(HandlerThread *handlerThread) : HwStreamMedia(handlerThread) {
    name = __FUNCTION__;
    this->lock = new SimpleLock();
    registerEvent(EVENT_COMMON_PREPARE, reinterpret_cast<EventFunc>(&HwAudioInput::eventPrepare));
    registerEvent(EVENT_AUDIO_START, reinterpret_cast<EventFunc>(&HwAudioInput::eventStart));
    registerEvent(EVENT_AUDIO_PAUSE, reinterpret_cast<EventFunc>(&HwAudioInput::eventPause));
    registerEvent(EVENT_AUDIO_STOP, reinterpret_cast<EventFunc>(&HwAudioInput::eventStop));
    registerEvent(EVENT_AUDIO_SEEK, reinterpret_cast<EventFunc>(&HwAudioInput::eventSeek));
    registerEvent(EVENT_AUDIO_LOOP, reinterpret_cast<EventFunc>(&HwAudioInput::eventLoop));
    registerEvent(EVENT_AUDIO_SET_SOURCE,
                  reinterpret_cast<EventFunc>(&HwAudioInput::eventSetSource));
    decoder = new AsynAudioDecoder();
}

HwAudioInput::~HwAudioInput() {
    LOGI("HwAudioInput::~HwAudioInput");
    lock->lock();
    if (decoder) {
        delete decoder;
        decoder = nullptr;
    }
    if (path) {
        delete[]path;
        path = nullptr;
    }
    lock->unlock();
    if (lock) {
        delete lock;
        lock = nullptr;
    }
}

bool HwAudioInput::eventPrepare(Message *msg) {
    playState = PAUSE;
    if (decoder->prepare(path)) {
//        createAudioPlayer();
    } else {
        LOGE("HwAudioInput::open %s failed", path);
        return true;
    }
    return false;
}

bool HwAudioInput::eventRelease(Message *msg) {
    LOGI("HwAudioInput::eventRelease");
    eventStop(nullptr);
    return false;
}

bool HwAudioInput::eventSetSource(Message *msg) {
    this->path = static_cast<char *>(msg->tyrUnBox());
    return false;
}

bool HwAudioInput::eventStart(Message *msg) {
    LOGI("HwAudioInput::eventStart");
    if (STOP != playState) {
        playState = PLAYING;
        loop();
    }
    if (decoder) {
        decoder->start();
    }
    return false;
}

bool HwAudioInput::eventPause(Message *msg) {
    if (STOP != playState) {
        playState = PAUSE;
    }
    if (decoder) {
        decoder->pause();
    }
    return false;
}

bool HwAudioInput::eventStop(Message *msg) {
    playState = STOP;
    if (decoder) {
        decoder->stop();
    }
    return false;
}

bool HwAudioInput::eventSeek(Message *msg) {
    int64_t us = msg->arg2;
    decoder->seek(us);
    return false;
}

bool HwAudioInput::eventLoop(Message *msg) {
    if (PLAYING != playState) {
        return false;
    }
    lock->lock();
    int ret = grab();
    lock->unlock();
    if (MEDIA_TYPE_EOF == ret) {
        eventStop(nullptr);
        return false;
    }
    loop();
    return false;
}

void HwAudioInput::loop() {
    postEvent(new Message(EVENT_AUDIO_LOOP, nullptr));
}

int HwAudioInput::grab() {
    int64_t time = getCurrentTimeUS();
    HwAbsMediaFrame *frame = nullptr;
    int ret = decoder->grab(&frame);
//    Logcat::i("HWVC", "HwAudioInput::grab cost: %lld, ret: %d", getCurrentTimeUS() - time, ret);
    if (!frame) {
        return ret;
    }
    int64_t curPts = frame->getPts();
    if (frame->isAudio()) {
        playFrame(dynamic_cast<HwAudioFrame *>(frame));
        return MEDIA_TYPE_AUDIO;
    }
    return ret;
}

void HwAudioInput::playFrame(HwAudioFrame *frame) {
    Message *msg = new Message(EVENT_SPEAKER_FEED, nullptr);
    msg->obj = frame;
    postEvent(msg);
}