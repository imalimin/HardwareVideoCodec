/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/
#include "../include/Echoer.h"

Echoer::Echoer(int channels, int sampleHz, int format, int minBufferSize) {
    this->minBufferSize = minBufferSize;
    this->buffer = new uint8_t[minBufferSize];
    this->engine = new SLEngine();
    recorder = new HwAudioRecorder(engine, channels, sampleHz, format, minBufferSize);
    player = new HwAudioPlayer(engine, channels, sampleHz, format, minBufferSize);
    this->pipeline = new EventPipeline("Echoer");
}

Echoer::~Echoer() {
    stop();
}

void Echoer::start() {
    running = true;
    pipeline->queueEvent([this] {
        if (player) {
            player->start();
        }
        if (recorder) {
            recorder->start();
        }
        loop();
    });
}

void Echoer::stop() {
    running = false;
    if (pipeline) {
        delete pipeline;
        pipeline = nullptr;
    }
    if (player) {
        player->stop();
        delete player;
        player = nullptr;
    }
    if (recorder) {
        recorder->stop();
        delete recorder;
        recorder = nullptr;
    }
    if (engine) {
        delete engine;
        engine = nullptr;
    }
    if (buffer) {
        delete[] buffer;
        buffer = nullptr;
    }
    minBufferSize = 0;
}

void Echoer::loop() {
    pipeline->queueEvent([this] {
        recorder->read(buffer);
        if (player) {
            player->write(buffer, minBufferSize);
        }
        if (this->running) {
            this->loop();
        }
    });
}
