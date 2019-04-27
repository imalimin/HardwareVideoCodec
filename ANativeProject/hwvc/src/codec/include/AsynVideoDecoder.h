/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_ASYNVIDEODECODER_H
#define HARDWAREVIDEOCODEC_ASYNVIDEODECODER_H

#include "AbsVideoDecoder.h"
#include "DefaultVideoDecoder.h"
#include "Frame.h"
#include "RecyclerBlockQueue.h"
#include "EventPipeline.h"
#include "HwAbsFrame.h"
#include "HwFrameAllocator.h"
#include "SimpleLock.h"
#include "PlayState.h"
#include <queue>

using namespace std;

#ifdef __cplusplus
extern "C" {
#endif

class AsynVideoDecoder : public AbsVideoDecoder, public AbsAudioDecoder {
public:
    AsynVideoDecoder();

    virtual ~AsynVideoDecoder();

    virtual bool prepare(string path) override;

    virtual int width() override;

    virtual int height() override;

    virtual int getChannels() override;

    virtual int getSampleHz() override;

    virtual int getSampleFormat() override;

    virtual int getPerSampleSize() override;

    virtual void seek(int64_t us) override;

    virtual void start();

    virtual void pause();

    int grab(HwAbsFrame **frame);

    virtual int64_t getVideoDuration() override;

    virtual int64_t getAudioDuration() override;

private:
    HwFrameAllocator *hwFrameAllocator = nullptr;
    DefaultVideoDecoder *decoder = nullptr;
    EventPipeline *pipeline = nullptr;
    queue<HwAbsFrame *> cache;
    HwAbsFrame *outputFrame = nullptr;//用于缓存一帧，以便在下次grab的时候进行回收
    PlayState playState = STOP;
    SimpleLock grabLock;

    void loop();

    /**
     * YUV420P
     */
    void copyYV12(Frame *dest, AVFrame *src);

    /**
     * YUV420SP
     */
    void copyNV12(Frame *dest, AVFrame *src);

    bool grab();

    bool grabAnVideoFrame();
};

#ifdef __cplusplus
}
#endif


#endif //HARDWAREVIDEOCODEC_ASYNVIDEODECODER_H
