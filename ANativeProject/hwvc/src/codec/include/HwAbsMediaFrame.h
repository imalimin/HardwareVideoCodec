/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */

#ifndef HARDWAREVIDEOCODEC_HWADBMEDIAFRAME_H
#define HARDWAREVIDEOCODEC_HWADBMEDIAFRAME_H

#include "HwAbsFrame.h"
#include "HwSourcesAllocator.h"

#ifdef __cplusplus
extern "C" {
#endif

#include "../include/ff/libavutil/samplefmt.h"

#ifdef __cplusplus
}
#endif

enum HwFrameFormat {
    HW_FMT_NONE = -1,
    HW_IMAGE_RGB,
    HW_IMAGE_RGBA,
    HW_IMAGE_YV12,     /** YUV420P */
    HW_IMAGE_NV12,     /** YUV420SP */
    HW_IMAGE_END,    /** End flag of image formats. DO NOT USE if linking dynamically */

    HW_SAMPLE_U8 = 100,/** unsigned 8 bits */
    HW_SAMPLE_S16,     /** signed 16 bits */
    HW_SAMPLE_S32,     /** signed 32 bits */
    HW_SAMPLE_FLT,     /** float */
    HW_SAMPLE_DBL,     /** double */

    HW_SAMPLE_U8P,     /** unsigned 8 bits, planar */
    HW_SAMPLE_S16P,    /** signed 16 bits, planar */
    HW_SAMPLE_S32P,    /** signed 32 bits, planar */
    HW_SAMPLE_FLTP,    /** float, planar */
    HW_SAMPLE_DBLP,    /** double, planar */
    HW_SAMPLE_S64,     /** signed 64 bits */
    HW_SAMPLE_S64P,    /** signed 64 bits, planar */
    HW_SAMPLE_END,    /** End flag of sample formats. DO NOT USE if linking dynamically */
};

class HwAbsMediaFrame : public HwSources, public HwAbsFrame {
public:
    /**
     * Convert to AVSampleFormat
     */
    static AVSampleFormat convertAudioFrameFormat(HwFrameFormat format);

    /**
     * Convert to HwFrameFormat
     */
    static HwFrameFormat convertToAudioFrameFormat(AVSampleFormat format);

    /**
     * For audio
     */
    static int getBytesPerSample(HwFrameFormat format);

    /**
     * For video
     */
    static int getImageSize(HwFrameFormat format, int width, int height);

public:

    HwAbsMediaFrame(HwSourcesAllocator *allocator, HwFrameFormat format, size_t size);

    virtual ~HwAbsMediaFrame();

    void setFormat(HwFrameFormat format);

    HwFrameFormat getFormat();

    void setPts(int64_t pts);

    int64_t getPts();

    bool isVideo();

    bool isAudio();

    /**
     * us
     */
    virtual uint64_t duration()=0;

    virtual HwAbsMediaFrame *clone()=0;

    virtual void clone(HwAbsMediaFrame *src)=0;

private:
    HwFrameFormat format = HW_FMT_NONE;
    int64_t pts;
};


#endif //HARDWAREVIDEOCODEC_HWADBMEDIAFRAME_H
