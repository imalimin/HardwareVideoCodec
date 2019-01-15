/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */

#ifndef HARDWAREVIDEOCODEC_ABSAUDIODECODER_H
#define HARDWAREVIDEOCODEC_ABSAUDIODECODER_H

#include "AbsDecoder.h"

class AbsAudioDecoder : virtual public AbsDecoder {
public:
    AbsAudioDecoder();

    virtual ~AbsAudioDecoder();

    virtual bool prepare(string path)=0;

    virtual void seek(int64_t us)=0;

    virtual int getChannels()=0;

    virtual int getSampleHz()=0;

    virtual int getSampleFormat()=0;

    virtual int getPerSampleSize()=0;
};


#endif //HARDWAREVIDEOCODEC_ABSAUDIODECODER_H
