//
// Created by mingyi.li on 2018/12/25.
//

#include "../include/Unit.h"
#include "log.h"

Unit::Unit() {

}

Unit::~Unit() {
    name = __func__;
}

void Unit::release() {

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
    return false;
}
