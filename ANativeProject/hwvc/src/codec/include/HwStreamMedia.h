/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */

#ifndef HARDWAREVIDEOCODEC_HWSTREAMMEDIA_H
#define HARDWAREVIDEOCODEC_HWSTREAMMEDIA_H

#include "Unit.h"

class HwStreamMedia : public Unit {
public:
    HwStreamMedia();

    HwStreamMedia(HandlerThread *handlerThread);

    virtual ~HwStreamMedia();

    virtual bool eventRelease(Message *msg)=0;

    virtual bool eventStart(Message *msg) = 0;

    virtual bool eventPause(Message *msg) = 0;

    virtual bool eventSeek(Message *msg) = 0;

    virtual bool eventStop(Message *msg) = 0;

    virtual bool eventSetSource(Message *msg) = 0;

};


#endif //HARDWAREVIDEOCODEC_HWSTREAMMEDIA_H
