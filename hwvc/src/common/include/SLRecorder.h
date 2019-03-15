/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/
#ifndef HARDWAREVIDEOCODEC_SLRECORDER_H
#define HARDWAREVIDEOCODEC_SLRECORDER_H

#include "Object.h"
#include "SLEngine.h"
#include "HwResult.h"
#include "AudioDevice.h"

class SLRecorder : public Object {
public:
    SLRecorder(SLEngine *engine);

    ~SLRecorder();

    HwResult initialize(SLAudioDevice *device);

    HwResult registerCallback(slAndroidSimpleBufferQueueCallback callback,
                              void *pContext);

    HwResult start();

    HwResult stop();

    HwResult clear();

    SLAndroidSimpleBufferQueueItf getQueue();

private:
    SLEngine *engine = nullptr;
    SLObjectItf recordObject = nullptr;
    SLRecordItf recordItf = nullptr;
    SLAndroidSimpleBufferQueueItf bufferQueueItf = nullptr;

    void release();

};


#endif //HARDWAREVIDEOCODEC_SLRECORDER_H
