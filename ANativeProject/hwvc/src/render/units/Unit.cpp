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
    LOGE("~Event");
    this->what = 0;
    this->handler = nullptr;
}

bool Event::dispatch(Unit *unit, Message *msg) {
    return (unit->*handler)(msg);
}

Unit::Unit() : Unit(nullptr) {
}

Unit::Unit(HandlerThread *handlerThread) {
    name = __FUNCTION__;
    registerEvent(EVENT_COMMON_RELEASE, reinterpret_cast<EventFunc>(&Unit::eventRelease));
    if (handlerThread) {
        eventPipeline = new EventPipeline(handlerThread);
    }
}

Unit::~Unit() {
    LOGI("~Unit(%s)", name.c_str());
    if (eventMap.empty()) return;
    for (auto itr = eventMap.rbegin(); itr != eventMap.rend(); itr++) {
//        delete itr->second;
    }
    eventMap.clear();
    if (eventPipeline) {
        delete eventPipeline;
        eventPipeline = nullptr;
    }
}

bool Unit::registerEvent(int what, EventFunc handler) {
    eventMap.insert(pair<int, Event *>(what, new Event(what, handler)));
    return true;
}

void Unit::setController(UnitPipeline *pipeline) {
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

void Unit::post(function<void()> runnable) {
    if (runnable) {
        if (eventPipeline) {
            eventPipeline->queueEvent(runnable);
        } else {
            runnable();
        }
    }
}
