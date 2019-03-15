/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/
#ifndef HARDWAREVIDEOCODEC_AUDIODEVICE_H
#define HARDWAREVIDEOCODEC_AUDIODEVICE_H

#include "Object.h"
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

class AudioDevice : public Object {
public:
    AudioDevice(uint16_t channels, uint32_t sampleRate, uint16_t format, uint32_t samplesPerBuffer);

    uint32_t getBufferByteSize();

protected:
    uint16_t channels = 0;
    uint32_t sampleRate = 0;
    uint16_t format = SL_PCMSAMPLEFORMAT_FIXED_16;
    uint32_t samplesPerBuffer = 0;
};

class SLAudioDevice : public AudioDevice {
public:
    SLAudioDevice(uint16_t channels, uint32_t sampleRate, uint16_t format,
                  uint32_t samplesPerBuffer);

    SLuint32 getChannelMask(int channels);

    void getSampleFormat(SLDataFormat_PCM *pFormat,
                         int format,
                         int channels,
                         int sampleRate);
};


#endif //HARDWAREVIDEOCODEC_AUDIODEVICE_H
