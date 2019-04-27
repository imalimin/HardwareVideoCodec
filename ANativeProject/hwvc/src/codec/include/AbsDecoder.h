/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_ABSDECODER_H
#define HARDWAREVIDEOCODEC_ABSDECODER_H

#include "Object.h"
#include <string>

using namespace std;

const int MEDIA_TYPE_UNKNOWN = -1;
const int MEDIA_TYPE_EOF = 0;
const int MEDIA_TYPE_VIDEO = 1;
const int MEDIA_TYPE_AUDIO = 2;

class AbsDecoder : public Object {
public:
    AbsDecoder();

    virtual ~AbsDecoder();

    virtual bool prepare(string path)=0;

    virtual void seek(int64_t us)=0;

};


#endif //HARDWAREVIDEOCODEC_ABSDECODER_H
