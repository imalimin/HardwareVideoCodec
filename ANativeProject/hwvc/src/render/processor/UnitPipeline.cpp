//
// Created by mingyi.li on 2018/12/25.
//

#include "../include/UnitPipeline.h"
#include "../include/Unit.h"

UnitPipeline::UnitPipeline(string name) {
    pthread_mutex_init(&mutex, nullptr);
    available = true;
    pipeline = new HandlerThread(name);
}

UnitPipeline::~UnitPipeline() {
    release();
    if (pipeline) {
        delete pipeline;
        pipeline = nullptr;
    }
    pthread_mutex_destroy(&mutex);
}

void UnitPipeline::release() {
    if (available) {
        pthread_mutex_lock(&mutex);
        available = false;
        Message *msg1 = new Message(EVENT_COMMON_RELEASE, nullptr);
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
        pthread_mutex_unlock(&mutex);
    }
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
        pthread_mutex_lock(&mutex);
        if (available) {
            pipeline->sendMessage(msg1);
        } else {
            delete msg1;
        }
        pthread_mutex_unlock(&mutex);
    } else {
        this->dispatch(msg1);
    }
}

void UnitPipeline::removeAllMessage(int what) {
    if (pipeline) {
        pipeline->removeAllMessage(what);
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
