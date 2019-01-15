/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_UNIT_H
#define HARDWAREVIDEOCODEC_UNIT_H

#include "Object.h"
#include "UnitPipeline.h"
#include "Message.h"
#include <map>

using namespace std;

static const int EVENT_COMMON_RELEASE = 0x0010000;
static const int EVENT_COMMON_PREPARE = 0x0010001;
static const int EVENT_COMMON_INVALIDATE = 0x0010002;

static const int EVENT_IMAGE_SHOW = 0x0020000;

static const int EVENT_RENDER_FILTER = 0x0030000;// 绘制滤镜
static const int EVENT_RENDER_SET_FILTER = 0x0030001;// 更新滤镜

static const int EVENT_SCREEN_DRAW = 0x0040000;

static const int EVENT_VIDEO_START = 0x0050000;
static const int EVENT_VIDEO_PAUSE = 0x0050001;
static const int EVENT_VIDEO_SEEK = 0x0050002;
static const int EVENT_VIDEO_SET_SOURCE = 0x0050003;

typedef bool (Unit::*EventFunc)(Message *);

class Event : public Object {
public:
    Event(int what, EventFunc handler);

    virtual ~Event();

    bool dispatch(Unit *unit, Message *msg);

protected:
    int what = 0;
    EventFunc handler;
};

class Unit : public Object {
public:
    Unit();

    virtual ~Unit();

    virtual void setController(UnitPipeline *pipeline);

    bool registerEvent(int what, EventFunc handler);

    virtual void release();

    /**
     * @msg 事件消息
     * @return true:我可以处理这个事件，false:无法处理这个事件
     */
    bool dispatch(Message *msg);

protected:
    string name;

    void postEvent(Message *msg);

private:
    map<int, Event *> eventMap;
    UnitPipeline *pipeline = nullptr;
};


#endif //HARDWAREVIDEOCODEC_UNIT_H
