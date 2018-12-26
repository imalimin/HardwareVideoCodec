/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_UNIT_H
#define HARDWAREVIDEOCODEC_UNIT_H

#include "Object.h"
#include "MainPipeline.h"
#include "Message.h"

#define EVENT_COMMON_PREPARE 0x0010001
#define EVENT_COMMON_RELEASE 0x0010002
#define EVENT_SCREEN_DRAW 0x0010003

#define EVENT_IMAGE_SHOW 0x0010004

class Unit : public Object {
public:
    Unit();

    virtual ~Unit();

    virtual void setController(MainPipeline *pipeline);

    /**
     * @msg 事件消息
     * @return true:我可以处理这个事件，false:无法处理这个事件
     */
    virtual bool dispatch(Message *msg);

    virtual void release();

protected:
    string name;

    void postEvent(Message *msg);

private:
    MainPipeline *pipeline = nullptr;
};


#endif //HARDWAREVIDEOCODEC_UNIT_H
