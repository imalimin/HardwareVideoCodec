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

class AudioRecorder : public SLAudioDevice {
public:
    AudioRecorder(unsigned int channels, unsigned int sampleHz, int format, int minBufferSize);

    virtual ~AudioRecorder();

    virtual int start();

    virtual void stop();

    virtual size_t read(uint8_t *buffer);

    virtual void flush();

    void bufferDequeue(SLBufferQueueItf slBufferQueueItf);

private:
    unsigned int channels = 0;
    unsigned int sampleHz = 0;
    SLuint32 format = SL_PCMSAMPLEFORMAT_FIXED_16;
    int minBufferSize = 0;
    RecyclerBlockQueue<ObjectBox> *recycler = nullptr;

    SLObjectItf engineObject = nullptr;
    SLEngineItf engineItf = nullptr;

    SLObjectItf recordObject = nullptr;
    SLRecordItf recordItf = nullptr;
    SLBufferQueueItf bufferQueueItf = nullptr;

    int createEngine();

    void destroyEngine();

    int createBufferQueueObject();

};


#endif //HARDWAREVIDEOCODEC_AUDIORECORDER_H
