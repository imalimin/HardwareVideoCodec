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

    int grab(Frame *frame);

private:
    DefaultVideoDecoder *decoder = nullptr;
    RecyclerBlockQueue<AVFrame> *vRecycler = nullptr;
    EventPipeline *pipeline = nullptr;
    bool lopping = true;

    void loop();

    /**
     * YUV420P
     */
    void copyYV12(Frame *dest, AVFrame *src);

    /**
     * YUV420SP
     */
    void copyNV12(Frame *dest, AVFrame *src);
};

#ifdef __cplusplus
}
#endif


#endif //HARDWAREVIDEOCODEC_ASYNVIDEODECODER_H
