//
// Created by mingyi.li on 2018/12/25.
//

#ifndef HARDWAREVIDEOCODEC_MAINPIPELINE_H
#define HARDWAREVIDEOCODEC_MAINPIPELINE_H

#include "Object.h"
#include "Message.h"
#include "HandlerThread.h"
#include <vector>

using namespace std;

class Unit;

class UnitPipeline : public Object {
public:
    UnitPipeline(string name);

    virtual ~UnitPipeline();

    void postEvent(Message *msg);

    void removeAllMessage(int what);

    int registerAnUnit(Unit *unit);

    void release();

private:
    HandlerThread *pipeline = nullptr;
    pthread_mutex_t mutex;
    vector<Unit *> units;
    bool available = true;

    void dispatch(Message *msg);

    void clear();
};


#endif //HARDWAREVIDEOCODEC_MAINPIPELINE_H
