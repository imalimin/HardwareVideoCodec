//
// Created by mingyi.li on 2018/12/25.
//

#ifndef HARDWAREVIDEOCODEC_UNIT_H
#define HARDWAREVIDEOCODEC_UNIT_H

#include "Object.h"
#include "MainPipeline.h"
#include "Message.h"

#define EVENT_PIPELINE_PREPARE 0x0010001
#define EVENT_PIPELINE_RELEASE 0x0010002
#define EVENT_PIPELINE_DRAW_SCREEN 0x0010003

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
