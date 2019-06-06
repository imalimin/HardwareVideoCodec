/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/
#ifndef HARDWAREVIDEOCODEC_AUDIORECORDER_H
#define HARDWAREVIDEOCODEC_AUDIORECORDER_H

#include <string>
#include "AudioDevice.h"
#include "ObjectBox.h"
#include "RecyclerBlockQueue.h"
#include "EventPipeline.h"
#include "SimpleLock.h"
#include "SLEngine.h"
#include "SLRecorder.h"
#include "HwResult.h"

class HwAudioRecorder : public SLAudioDevice {
public:
    HwAudioRecorder(uint16_t channels,
                  uint32_t sampleRate,
                  uint16_t format,
                  uint32_t samplesPerBuffer);

    HwAudioRecorder(SLEngine *engine,
                  uint16_t channels,
                  uint32_t sampleRate,
                  uint16_t format,
                  uint32_t samplesPerBuffer);

    virtual ~HwAudioRecorder();

    virtual HwResult start();

    virtual void stop();

    virtual size_t read(uint8_t *buffer);

    virtual void flush();

    void bufferDequeue(SLAndroidSimpleBufferQueueItf slBufferQueueItf);

private:
    RecyclerBlockQueue<ObjectBox> *recycler = nullptr;

    SLEngine *engine = nullptr;
    bool ownEngine = false;
    SLRecorder *recorder = nullptr;
    ObjectBox *buffer = nullptr;
    FILE *pcmFile = nullptr;

    HwResult createEngine();

    void destroyEngine();

    HwResult createBufferQueueObject();

    void initialize(SLEngine *engine);

};


#endif //HARDWAREVIDEOCODEC_AUDIORECORDER_H
