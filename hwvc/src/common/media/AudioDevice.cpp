/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/
#include <assert.h>
#include "../include/AudioDevice.h"

AudioDevice::AudioDevice(uint16_t channels,
                         uint32_t sampleRate,
                         uint16_t format,
                         uint32_t samplesPerBuffer) {
    this->channels = channels;
    this->sampleRate = sampleRate;
    this->format = format;
    this->samplesPerBuffer = samplesPerBuffer;
}

uint32_t AudioDevice::getBufferByteSize() {
    uint32_t bufSize = samplesPerBuffer * channels * format;
    bufSize = (bufSize + 7) >> 3;  // bits --> byte
    return bufSize;
}

SLAudioDevice::SLAudioDevice(uint16_t channels,
                             uint32_t sampleRate,
                             uint16_t format,
                             uint32_t samplesPerBuffer) : AudioDevice(channels,
                                                                      sampleRate,
                                                                      format,
                                                                      samplesPerBuffer) {
}

SLuint32 SLAudioDevice::getChannelMask(int channels) {
    switch (channels) {
        case 1:
            return SL_SPEAKER_FRONT_LEFT;
        case 3:
            return SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT | SL_SPEAKER_FRONT_CENTER;
        case 2:
        default:
            return SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT;
    }
}

void SLAudioDevice::getSampleFormat(SLDataFormat_PCM *pFormat,
                                    int format,
                                    int channels,
                                    int sampleRate) {
    memset(pFormat, 0, sizeof(*pFormat));

    pFormat->formatType = SL_DATAFORMAT_PCM;
    pFormat->numChannels = static_cast<SLuint32>(channels);
    pFormat->channelMask = getChannelMask(channels);
    pFormat->samplesPerSec = static_cast<SLuint32>(sampleRate);

    pFormat->endianness = SL_BYTEORDER_LITTLEENDIAN;
    pFormat->bitsPerSample = static_cast<SLuint32>(format);
    pFormat->containerSize = static_cast<SLuint32>(format);
}