/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "DeNoise.h"

DeNoise::DeNoise(int sampleRate, int sampleSize) {
    this->sampleRate = sampleRate;
    this->sampleSize = sampleSize;
    this->buffer = static_cast<spx_int16_t *>(malloc(sizeof(spx_int16_t) * this->sampleRate));
    st = speex_preprocess_state_init(this->sampleSize, this->sampleRate);
    int denoise = 1;
    int noiseSuppress = DENOISE_DB;
    speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_DENOISE, &denoise); //降噪
    speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_NOISE_SUPPRESS, &noiseSuppress); //设置噪声的dB
//    int i = 0;
//    speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_AGC, &i);
//    i = 8000;
//    speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_AGC_LEVEL, &i);
//    i = 0;
//    speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_DEREVERB, &i);
//    float f = .0;
//    speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_DEREVERB_DECAY, &f);
//    f = .0;
//    speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_DEREVERB_LEVEL, &f);
//    int vad = 1;
//    int vadProbStart = 80;
//    int vadProbContinue = 65;
//    speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_VAD, &vad); //静音检测
//    speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_PROB_START,
//                         &vadProbStart); //Set probability required for the VAD to go from silence to voice
//    speex_preprocess_ctl(st, SPEEX_PREPROCESS_SET_PROB_CONTINUE,
//                         &vadProbContinue); //Set probability required for the VAD to stay in the voice state (integer percent)
}

int DeNoise::preprocess(char *data) {
    if (NULL == st) {
        return -1;
    }
    setBuffer(this->buffer, data);
    int vad = speex_preprocess_run(st, this->buffer);
    getBuffer(data, this->buffer);
    if (vad != 0) {//speech
        return TYPE_SPEECH;
    } else {//slience
        return TYPE_SLIENCE;
    }
}

void DeNoise::setBuffer(spx_int16_t *dest, char *src) {
    for (int i = 0; i < this->sampleSize; i++) {
        this->buffer[i] = (short) (((0x000000FF & src[i * 2]) << 8) |
                                   (0x000000FF & src[i * 2 + 1]));
    }
}

void DeNoise::getBuffer(char *dest, spx_int16_t *src) {
    for (int i = 0; i < this->sampleSize; i++) {
//        this->buffer[i] = (short) ((src[i * 2] << 8) + (src[i * 2 + 1] << 0));
        dest[i * 2] = (char) ((this->buffer[i] & 0xFF00) >> 8);
        dest[i * 2 + 1] = (char) (this->buffer[i] & 0xFF);
    }
}

DeNoise::~DeNoise() {
    speex_preprocess_state_destroy(st);
    if (NULL != buffer)
        free(buffer);
}
