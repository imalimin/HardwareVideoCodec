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
    recorder = new AudioRecorder(channels, sampleHz, format, minBufferSize);
    player = new AudioPlayer(channels, sampleHz, format, minBufferSize);
    this->pipeline = new EventPipeline("Echoer");
}

Echoer::~Echoer() {
    stop();
}

void Echoer::start() {
    running = true;
    pipeline->queueEvent([this] {
        if (recorder) {
            recorder->start();
        }
        if (player) {
            player->start();
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
    if (buffer) {
        delete[] buffer;
        buffer = nullptr;
    }
    minBufferSize = 0;
}

void Echoer::loop() {
    pipeline->queueEvent([this] {
        recorder->read(buffer);
        player->write(buffer, minBufferSize);
        if (this->running) {
            this->loop();
        }
    });
}
