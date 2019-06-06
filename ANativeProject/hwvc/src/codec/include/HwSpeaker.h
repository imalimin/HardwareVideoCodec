/*
* Copyright (c) 2018-present, lmyooyo@gmail.com.
*
* This source code is licensed under the GPL license found in the
* LICENSE file in the root directory of this source tree.
*/

#ifndef HARDWAREVIDEOCODEC_HWSPEAKER_H
#define HARDWAREVIDEOCODEC_HWSPEAKER_H

#include "Unit.h"
#include "HwAudioPlayer.h"
#include "HwAudioFrame.h"

class HwSpeaker : public Unit {
public:
    HwSpeaker();

    HwSpeaker(HandlerThread *handlerThread);

    virtual ~HwSpeaker();

    bool eventRelease(Message *msg);

    bool eventPrepare(Message *msg);

    bool eventFeed(Message *msg);

private:
    void createFromAudioFrame(HwAudioFrame *frame);

private:
    HwAudioPlayer *player = nullptr;

};


#endif //HARDWAREVIDEOCODEC_HWSPEAKER_H
