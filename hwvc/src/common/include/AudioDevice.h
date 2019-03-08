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

class AudioDevice : Object {

};

class SLAudioDevice : AudioDevice {
public:
    SLuint32 getChannelMask(int channels);
};


#endif //HARDWAREVIDEOCODEC_AUDIODEVICE_H
