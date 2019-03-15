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

using namespace std;

class AudioPlayer : public SLAudioDevice {
public:
    AudioPlayer(int channels, int sampleHz, int format, int minBufferSize);

    AudioPlayer(SLEngine *engine, int channels, int sampleHz, int format, int minBufferSize);

    virtual ~AudioPlayer();

    virtual HwResult start();

    virtual void stop();

    virtual HwResult write(uint8_t *buffer, size_t size);

    virtual void flush();

    void bufferEnqueue(SLAndroidSimpleBufferQueueItf slBufferQueueItf);

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
    SLAndroidSimpleBufferQueueItf bufferQueueItf = nullptr;

    HwResult createEngine();

    void destroyEngine();

    HwResult createBufferQueueAudioPlayer();

    void initialize(SLEngine *engine, int channels, int sampleHz, int format, int minBufferSize);
};


#endif //HARDWAREVIDEOCODEC_AUDIOPLAYER_H
