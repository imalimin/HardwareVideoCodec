/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/
#ifndef HARDWAREVIDEOCODEC_AUDIOPLAYER_H
#define HARDWAREVIDEOCODEC_AUDIOPLAYER_H

#include <string>
#include "AudioDevice.h"
#include "ObjectBox.h"
#include "RecyclerBlockQueue.h"
#include "EventPipeline.h"
#include "SimpleLock.h"
#include "SLEngine.h"
#include "HwResult.h"
#include <queue>
#include "HwFIFOBuffer.h"
#include "HwLogEnable.h"

using namespace std;

class AudioPlayer : public SLAudioDevice, HwLogEnable {
public:
    AudioPlayer(uint16_t channels,
                uint32_t sampleRate,
                uint16_t format,
                uint32_t samplesPerBuffer);

    AudioPlayer(SLEngine *engine,
                uint16_t channels,
                uint32_t sampleRate,
                uint16_t format,
                uint32_t samplesPerBuffer);

    virtual ~AudioPlayer();

    virtual HwResult start();

    virtual void stop();

    virtual HwResult write(uint8_t *buffer, size_t size);

    virtual void flush();

    void bufferEnqueue(SLAndroidSimpleBufferQueueItf slBufferQueueItf);

private:
    SimpleLock lock;
    std::queue<ObjectBox *> pcmList;
    HwFIFOBuffer *fifo = nullptr;
    SLEngine *engine = nullptr;
    bool ownEngine = false;
    SLObjectItf mixObject = nullptr;
    SLObjectItf playObject = nullptr;
    SLPlayItf playItf = nullptr;
    SLAndroidSimpleBufferQueueItf bufferQueueItf = nullptr;
    FILE *file = nullptr;

    HwResult createEngine();

    void destroyEngine();

    HwResult createBufferQueueAudioPlayer();

    void initialize(SLEngine *engine);
};


#endif //HARDWAREVIDEOCODEC_AUDIOPLAYER_H
