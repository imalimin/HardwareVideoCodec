/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/

#ifndef HARDWAREVIDEOCODEC_ASYNAUDIODECODER_H
#define HARDWAREVIDEOCODEC_ASYNAUDIODECODER_H

#include "AbsVideoDecoder.h"
#include "DefaultAudioDecoder.h"
#include "EventPipeline.h"
#include "HwAbsFrame.h"
#include "HwFrameAllocator.h"
#include "SimpleLock.h"
#include "PlayState.h"
#include <queue>

using namespace std;

class AsynAudioDecoder : public AbsAudioDecoder {
public:
    AsynAudioDecoder();

    virtual ~AsynAudioDecoder();

    bool prepare(string path);

    void seek(int64_t us);

    void start();

    void pause();

    void stop();

    /**
     * @return 1: video, 2: audio, 0: failed
     */
    int grab(HwAbsFrame **frame);

    int getChannels();

    int getSampleHz();

    int getSampleFormat();

    int getPerSampleSize();

    int64_t getAudioDuration();

private:
    void loop();

    bool grab();

private:
    HwFrameAllocator *hwFrameAllocator = nullptr;
    DefaultAudioDecoder *decoder = nullptr;
    EventPipeline *pipeline = nullptr;
    queue<HwAbsFrame *> cache;
    HwAbsFrame *outputFrame = nullptr;//用于缓存一帧，以便在下次grab的时候进行回收
    PlayState playState = STOP;
    SimpleLock grabLock;
    FILE *file = nullptr;
};


#endif //HARDWAREVIDEOCODEC_ASYNAUDIODECODER_H
