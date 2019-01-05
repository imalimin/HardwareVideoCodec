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

class AsynVideoDecoder : public AbsVideoDecoder {
public:
    AsynVideoDecoder();

    virtual ~AsynVideoDecoder();

    bool prepare(string path) override;

    int width() override;

    int height() override;

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
