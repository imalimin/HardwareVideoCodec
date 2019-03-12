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

using namespace std;

class AudioPlayer : public SLAudioDevice {
public:
    AudioPlayer(int channels, int sampleHz, int format, int minBufferSize);

    AudioPlayer(SLEngine *engine, int channels, int sampleHz, int format, int minBufferSize);

    virtual ~AudioPlayer();

    virtual int start();

    virtual void stop();

    virtual int write(uint8_t *buffer, size_t size);

    virtual void flush();

    void bufferEnqueue(SLBufferQueueItf slBufferQueueItf);

private:
    SimpleLock *lock = nullptr;
    unsigned int channels = 0;
    unsigned int sampleHz = 0;
    SLuint32 format = SL_PCMSAMPLEFORMAT_FIXED_16;
    int minBufferSize = 0;
    RecyclerBlockQueue<ObjectBox> *recycler = nullptr;
    SLEngine *engine = nullptr;
    bool ownEngine = false;
    SLObjectItf mixObject = nullptr;
    SLObjectItf playObject = nullptr;
    SLPlayItf playItf = nullptr;
    SLBufferQueueItf bufferQueueItf = nullptr;

    int createEngine();

    void destroyEngine();

    int createBufferQueueAudioPlayer();

    void initialize(SLEngine *engine, int channels, int sampleHz, int format, int minBufferSize);
};


#endif //HARDWAREVIDEOCODEC_AUDIOPLAYER_H
