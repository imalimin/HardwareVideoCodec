/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/

#ifndef HARDWAREVIDEOCODEC_HWAUDIOINPUT_H
#define HARDWAREVIDEOCODEC_HWAUDIOINPUT_H

#include "HwStreamMedia.h"
#include "AsynAudioDecoder.h"
#include "HwAudioFrame.h"

class HwAudioInput : public HwStreamMedia {
public:
    HwAudioInput();

    HwAudioInput(HandlerThread *handlerThread);

    virtual ~HwAudioInput();

    bool eventRelease(Message *msg);

    bool eventPrepare(Message *msg);

    bool eventSetSource(Message *msg);

    bool eventStart(Message *msg);

    bool eventPause(Message *msg);

    bool eventStop(Message *msg);

    bool eventSeek(Message *msg);

    bool eventLoop(Message *msg);

private:
    void loop();

    int grab();

    void playFrame(HwAudioFrame *frame);

private:
    AsynAudioDecoder *decoder = nullptr;
    PlayState playState = STOP;
    SimpleLock simpleLock;
    SimpleLock playTimeLock;
    string path;
    HwAudioFrame *frame = nullptr;
};


#endif //HARDWAREVIDEOCODEC_HWAUDIOINPUT_H
