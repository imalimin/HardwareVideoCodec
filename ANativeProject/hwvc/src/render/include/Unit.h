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
#include "EventPipeline.h"
#include "Message.h"
#include <map>

#define KID(a, b, c, d) ((d) | ((c) << 8) | ((b) << 16) | ((unsigned)(a) << 24))

using namespace std;

static constexpr int EVENT_COMMON_RELEASE = KID('C', 'O', 'M', 0x01);
static constexpr int EVENT_COMMON_PREPARE = KID('C', 'O', 'M', 0x02);
static constexpr int EVENT_COMMON_INVALIDATE = KID('C', 'O', 'M', 0x03);

static constexpr int EVENT_IMAGE_SHOW = KID('I', 'M', 'G', 0x01);

static constexpr int EVENT_RENDER_FILTER = KID('R', 'N', 'D', 0x01);// 绘制滤镜
static constexpr int EVENT_RENDER_SET_FILTER = KID('R', 'N', 'D', 0x02);// 更新滤镜

static constexpr int EVENT_SCREEN_DRAW = KID('S', 'C', 'R', 0x01);

static constexpr int EVENT_VIDEO_START = KID('V', 'D', 'O', 0x01);
static constexpr int EVENT_VIDEO_PAUSE = KID('V', 'D', 'O', 0x02);
static constexpr int EVENT_VIDEO_SEEK = KID('V', 'D', 'O', 0x03);
static constexpr int EVENT_VIDEO_SET_SOURCE = KID('V', 'D', 'O', 0x04);
static constexpr int EVENT_VIDEO_LOOP = KID('V', 'D', 'O', 0x05);

static constexpr int EVENT_AUDIO_START = KID('A', 'D', 'O', 0x01);
static constexpr int EVENT_AUDIO_PAUSE = KID('A', 'D', 'O', 0x02);
static constexpr int EVENT_AUDIO_STOP = KID('A', 'D', 'O', 0x03);
static constexpr int EVENT_AUDIO_SEEK = KID('A', 'D', 'O', 0x04);
static constexpr int EVENT_AUDIO_SET_SOURCE = KID('A', 'D', 'O', 0x05);
static constexpr int EVENT_AUDIO_LOOP = KID('A', 'D', 'O', 0x06);

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

    Unit(HandlerThread *handlerThread);

    virtual ~Unit();

    virtual void setController(UnitPipeline *pipeline);

    bool registerEvent(int what, EventFunc handler);

    virtual bool eventRelease(Message *msg)=0;

    /**
     * @msg 事件消息
     * @return true:我可以处理这个事件，false:无法处理这个事件
     */
    bool dispatch(Message *msg);

    void post(function<void()> runnable);

protected:
    string name;

    void postEvent(Message *msg);

private:
    map<int, Event *> eventMap;
    UnitPipeline *pipeline = nullptr;
    EventPipeline *eventPipeline = nullptr;
};


#endif //HARDWAREVIDEOCODEC_UNIT_H
