//
// Created by mingyi.li on 2018/12/25.
//

#include "../include/MainPipeline.h"
#include "../include/Unit.h"

MainPipeline::MainPipeline(string name) {
    pipeline = new HandlerThread(name);
}

MainPipeline::~MainPipeline() {
    if (pipeline) {
        delete pipeline;
        pipeline = nullptr;
    }
//    for (auto itr = units.cbegin(); itr != units.cend(); itr++) {
//        delete *itr;
//    }
    units.clear();
}

void MainPipeline::postEvent(Message *msg) {
    if (pipeline) {
        msg->runnable = [=](Message *msg) {
            dispatch(msg);
        };
        pipeline->sendMessage(msg);
    }
}

void MainPipeline::dispatch(Message *msg) {
    for (auto itr = units.cbegin(); itr != units.cend(); itr++) {
        bool ret = (*itr)->dispatch(msg);
    }
}

int MainPipeline::registerAnUnit(Unit *unit) {
//    unit->setController(this);
    units.push_back(unit);
    return 1;
}
