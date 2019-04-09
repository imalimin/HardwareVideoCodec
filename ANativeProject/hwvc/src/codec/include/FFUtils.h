/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/

#ifndef HARDWAREVIDEOCODEC_FFUTILS_H
#define HARDWAREVIDEOCODEC_FFUTILS_H

#ifdef __cplusplus
extern "C" {
#endif

#include "ff/libavcodec/avcodec.h"
#include "ff/libavformat/avformat.h"
#include "ff/libavutil/avutil.h"
#include "ff/libswresample/swresample.h"

namespace FFUtils {
    int avSamplesCopy(AVFrame *dest, AVFrame *src);
}

#ifdef __cplusplus
}
#endif

#endif //HARDWAREVIDEOCODEC_FFUTILS_H
