/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <string.h>
#include <include/speex/speex_preprocess.h>

#define DENOISE_DB -25
#define TYPE_SPEECH 0x100
#define TYPE_SLIENCE 0x101
#ifndef HARDWAREVIDEOCODEC_DENOISE_H
#define HARDWAREVIDEOCODEC_DENOISE_H

class DeNoise {
public:
    DeNoise(int sampleRate, int sampleSize);

    int preprocess(char *data);

    ~DeNoise();

private:

    void setBuffer(spx_int16_t *dest, char *src);

    void getBuffer(char *dest, spx_int16_t *src);

    int sampleRate, sampleSize;
    SpeexPreprocessState *st = NULL;
    spx_int16_t *buffer = NULL;
};


#endif //HARDWAREVIDEOCODEC_DENOISE_H
