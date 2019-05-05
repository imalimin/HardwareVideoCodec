/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/

#include "../include/AudioProcessor.h"
#include "../include/HwAudioInput.h"
#include "ObjectBox.h"

AudioProcessor::AudioProcessor() : Object() {
    pipeline = new UnitPipeline("AudioProcessor");
    pipeline->registerAnUnit(new HwAudioInput());
}

AudioProcessor::~AudioProcessor() {
    if (pipeline) {
        pipeline->release();
        wait(20000);
        delete pipeline;
        pipeline = nullptr;
    }
}

void AudioProcessor::setSource(char *path) {
    if (pipeline) {
        Message *msg = new Message(EVENT_AUDIO_SET_SOURCE, nullptr);
        msg->obj = new ObjectBox(path);
        pipeline->postEvent(msg);
    }
}

void AudioProcessor::prepare() {
    if (pipeline) {
        Message *msg = new Message(EVENT_COMMON_PREPARE, nullptr);
        msg->obj = new ObjectBox(nullptr);
        pipeline->postEvent(msg);
    }
}

void AudioProcessor::start() {
    if (pipeline) {
        Message *msg = new Message(EVENT_AUDIO_START, nullptr);
        pipeline->postEvent(msg);
    }
}

void AudioProcessor::pause() {
    if (pipeline) {
        Message *msg = new Message(EVENT_AUDIO_PAUSE, nullptr);
        pipeline->postEvent(msg);
    }
}

void AudioProcessor::stop() {
    if (pipeline) {
        Message *msg = new Message(EVENT_AUDIO_STOP, nullptr);
        pipeline->postEvent(msg);
    }
}

void AudioProcessor::seek(int64_t us) {
    if (pipeline) {
        pipeline->removeAllMessage(EVENT_AUDIO_SEEK);
        Message *msg = new Message(EVENT_AUDIO_SEEK, nullptr);
        msg->arg2 = us;
        pipeline->postEvent(msg);
    }
}