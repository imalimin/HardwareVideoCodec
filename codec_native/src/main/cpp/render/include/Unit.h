//
// Created by mingyi.li on 2018/12/25.
//

#ifndef HARDWAREVIDEOCODEC_UNIT_H
#define HARDWAREVIDEOCODEC_UNIT_H

#include "Object.h"
#include "MainPipeline.h"
#include "Message.h"

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

protected:
    string name;

    void postEvent(Message *msg);

private:
    MainPipeline *pipeline = nullptr;
};


#endif //HARDWAREVIDEOCODEC_UNIT_H
