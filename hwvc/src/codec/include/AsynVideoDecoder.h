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

enum PlayState {
    PAUSE = 0,
    PLAYING = 1,
    STOP = -1
};

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

    int grab(Frame *frame);

    virtual int64_t getVideoDuration() override;

    virtual int64_t getAudioDuration() override;

private:
    DefaultVideoDecoder *decoder = nullptr;
    RecyclerBlockQueue<AVFrame> *vRecycler = nullptr;
    EventPipeline *pipeline = nullptr;
    PlayState playState = STOP;

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
