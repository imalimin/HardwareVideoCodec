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

class MainPipeline : public Object {
public:
    MainPipeline(string name);

    virtual ~MainPipeline();

    void postEvent(Message *msg);

    int registerAnUnit(Unit *unit);

private:
    HandlerThread *pipeline = nullptr;
    vector<Unit *> units;

    void dispatch(Message *msg);
};


#endif //HARDWAREVIDEOCODEC_MAINPIPELINE_H
