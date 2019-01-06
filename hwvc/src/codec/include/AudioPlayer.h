/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/
#ifndef HARDWAREVIDEOCODEC_AUDIOPLAYER_H
#define HARDWAREVIDEOCODEC_AUDIOPLAYER_H

#include <string>
#include "../log/log.h"
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <Object.h>

using namespace std;

class AudioPlayer : public Object {
public:
    AudioPlayer(int channels, int sampleHz);

    virtual ~AudioPlayer();

    virtual int start();

    virtual void stop();

    virtual int write(uint8_t *buffer, size_t size);

    string getString();

    uint8_t *readBuffer(size_t *size);

private:
    unsigned int channels = 0;
    unsigned int sampleHz = 0;
    bool requestRead = false;
    size_t size = 0;
    uint8_t *buffer = nullptr;
    size_t quietSize = 0;
    uint8_t *quietBuffer = nullptr;
    SLObjectItf engineObject = nullptr;
    SLEngineItf engineItf = nullptr;
    SLObjectItf mixObject = nullptr;
    SLObjectItf playObject = nullptr;
    SLPlayItf playItf = nullptr;
    SLBufferQueueItf bufferQueueItf = nullptr;
    slBufferQueueCallback callback = [](SLBufferQueueItf slBufferQueueItf, void *context) {
        AudioPlayer *player = static_cast<AudioPlayer *>(context);
        size_t size = 0;
        uint8_t *buf = player->readBuffer(&size);
//        LOGE("getQueueCallBack, size=%d", size);
        (*slBufferQueueItf)->Enqueue(slBufferQueueItf, buf, size);
    };

    int createEngine();

    void destroyEngine();

    int createBufferQueueAudioPlayer();
};


#endif //HARDWAREVIDEOCODEC_AUDIOPLAYER_H
