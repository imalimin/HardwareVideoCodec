//
// Created by mingyi.li on 2018/12/25.
//

#include "../include/Unit.h"
#include "log.h"

Event::Event(int what, EventFunc handler) {
    this->what = what;
    this->handler = handler;
}

Event::~Event() {
    this->what = 0;
    this->handler = nullptr;
}

bool Event::dispatch(Unit *unit, Message *msg) {
    return (unit->*handler)(msg);
}

Unit::Unit() {
    name = __func__;
}

Unit::~Unit() {
//    release();
}

void Unit::release() {
    if (eventMap.empty()) return;
    for (auto itr = eventMap.rbegin(); itr != eventMap.rend(); itr++) {
//        delete itr->second;
    }
    eventMap.clear();
}

bool Unit::registerEvent(int what, EventFunc handler) {
    eventMap.insert(pair<int, Event *>(what, new Event(what, handler)));
    return true;
}

void Unit::setController(MainPipeline *pipeline) {
    this->pipeline = pipeline;
}

void Unit::postEvent(Message *msg) {
    if (pipeline) {
        pipeline->postEvent(msg);
    } else {
        LOGE("%s`s pipeline is null", name.c_str());
    }
}

bool Unit::dispatch(Message *msg) {
    auto itr = eventMap.find(msg->what);
    if (eventMap.end() != itr) {
        return itr->second->dispatch(this, msg);
    }
    return false;
}
