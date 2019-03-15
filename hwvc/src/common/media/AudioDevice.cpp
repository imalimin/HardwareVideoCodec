/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/
#include <assert.h>
#include "../include/AudioDevice.h"

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