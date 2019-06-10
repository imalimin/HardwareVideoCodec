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
#include "EventPipeline.h"
#include "SimpleLock.h"
#include "SLEngine.h"
#include "SLRecorder.h"
#include "HwResult.h"
#include "HwFIFOBuffer.h"
#include "HwBuffer.h"

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

    virtual HwBuffer *read(size_t size);

    virtual void flush();

    void bufferDequeue(SLAndroidSimpleBufferQueueItf slBufferQueueItf);

private:
    SLEngine *engine = nullptr;
    bool ownEngine = false;
    SLRecorder *recorder = nullptr;
    HwFIFOBuffer *fifo = nullptr;
    HwBuffer *buffer = nullptr;
    FILE *pcmFile = nullptr;

    HwResult createEngine();

    void destroyEngine();

    HwResult createBufferQueueObject();

    void initialize(SLEngine *engine);

};


#endif //HARDWAREVIDEOCODEC_AUDIORECORDER_H
