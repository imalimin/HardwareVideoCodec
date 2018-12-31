//
// Created by mingyi.li on 2018/12/25.
//

#include "../include/UnitPipeline.h"
#include "../include/Unit.h"

UnitPipeline::UnitPipeline(string name) {
    pipeline = new HandlerThread(name);
}

UnitPipeline::~UnitPipeline() {
    Message *msg = new Message(EVENT_COMMON_RELEASE, nullptr);
    postEvent(msg);
    if (pipeline) {
        delete pipeline;
        pipeline = nullptr;
    }
}

void UnitPipeline::release() {
}

void UnitPipeline::postEvent(Message *msg1) {
    if (pipeline) {
        msg1->runnable = [this](Message *msg2) {
            /**
             * @NOTE 不置空的话会出现不可预料的崩溃
             */
            msg2->runnable = nullptr;
            this->dispatch(msg2);
        };
        pipeline->sendMessage(msg1);
    }
}

void UnitPipeline::dispatch(Message *msg) {
    for (auto itr = units.cbegin(); itr != units.cend(); itr++) {
        bool ret = (*itr)->dispatch(msg);
    }
    if (EVENT_COMMON_RELEASE == msg->what) {
        clear();
    }
}

void UnitPipeline::clear() {
    LOGI("UnitPipeline::clear units");
    for (auto unit:units) {
        delete unit;
    }
    units.clear();
}

int UnitPipeline::registerAnUnit(Unit *unit) {
    unit->setController(this);
    units.push_back(unit);
    return 1;
}
