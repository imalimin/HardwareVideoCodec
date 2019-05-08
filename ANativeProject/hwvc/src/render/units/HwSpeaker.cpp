/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/

#include <ff/libavutil/samplefmt.h>
#include "../include/HwSpeaker.h"

HwSpeaker::HwSpeaker() : Unit() {
    name = __FUNCTION__;
    registerEvent(EVENT_COMMON_PREPARE, reinterpret_cast<EventFunc>(&HwSpeaker::eventPrepare));
    registerEvent(EVENT_SPEAKER_FEED, reinterpret_cast<EventFunc>(&HwSpeaker::eventFeed));
}

HwSpeaker::HwSpeaker(HandlerThread *handlerThread) : Unit(handlerThread) {
    name = __FUNCTION__;
    registerEvent(EVENT_COMMON_PREPARE, reinterpret_cast<EventFunc>(&HwSpeaker::eventPrepare));
    registerEvent(EVENT_SPEAKER_FEED, reinterpret_cast<EventFunc>(&HwSpeaker::eventFeed));
}

HwSpeaker::~HwSpeaker() {
    LOGI("HwSpeaker::~HwSpeaker");
    if (audioPlayer) {
        audioPlayer->stop();
        delete audioPlayer;
        audioPlayer = nullptr;
    }
}

bool HwSpeaker::eventPrepare(Message *msg) {
    return false;
}

bool HwSpeaker::eventRelease(Message *msg) {
    return false;
}

bool HwSpeaker::eventFeed(Message *msg) {
    if (msg->obj) {
        HwAudioFrame *frame = dynamic_cast<HwAudioFrame *>(msg->obj);
        createFromAudioFrame(frame);
        if (audioPlayer) {
            Logcat::i("HWVC", "HwAudioInput::play audio: %d, %d, %lld, %lld",
                      frame->getChannels(),
                      frame->getSampleRate(),
                      frame->getSampleCount(),
                      frame->getDataSize());
            audioPlayer->write(frame->getData(), static_cast<size_t>(frame->getDataSize()));
        }
    }
    return false;
}

void HwSpeaker::createFromAudioFrame(HwAudioFrame *frame) {
    if (audioPlayer) {
        return;
    }
    int format;
    switch (frame->getFormat()) {
        case AV_SAMPLE_FMT_S16:
            format = SL_PCMSAMPLEFORMAT_FIXED_16;
            break;
        case AV_SAMPLE_FMT_U8:
            format = SL_PCMSAMPLEFORMAT_FIXED_8;
            break;
        default:
            format = SL_PCMSAMPLEFORMAT_FIXED_32;
    }
    audioPlayer = new AudioPlayer(frame->getChannels(),
                                  frame->getSampleRate(),
                                  static_cast<uint16_t>(format),
                                  static_cast<uint32_t>(frame->getSampleCount()));
    audioPlayer->start();
}