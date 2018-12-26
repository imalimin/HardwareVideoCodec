/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <log.h>
#include "../include/Render.h"

//void Render::post() {
//    pipeline->queueEvent([=] {
//        ++count;
//        LOGI("Handle %d", count);
//    });
//}

Render::Render() {
    name = __func__;
//    pipeline = new EventPipeline("Render");
}

Render::~Render() {
//    if (nullptr != pipeline) {
//        delete pipeline;
//        pipeline = nullptr;
//    }
}

bool Render::dispatch(Message *msg) {
    return Unit::dispatch(msg);
}
