/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/
#ifndef HARDWAREVIDEOCODEC_EOCHER_H
#define HARDWAREVIDEOCODEC_EOCHER_H

#include "AudioPlayer.h"
#include "AudioRecorder.h"
#include "EventPipeline.h"

class Echoer : public Object {
public:
    Echoer(int channels, int sampleHz, int format, int minBufferSize);

    ~Echoer();

    void start();

    void stop();

private:
    int minBufferSize = 0;
    AudioPlayer *player;
    AudioRecorder *recorder;
    EventPipeline *pipeline = nullptr;
    uint8_t *buffer = nullptr;
    bool running = false;

    void loop();
};


#endif //HARDWAREVIDEOCODEC_EOCHER_H
