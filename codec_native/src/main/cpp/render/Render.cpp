/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <log.h>
#include "Render.h"

Render::Render() {
    pipeline = new EventPipeline("Render");
}

Render::~Render() {
    if (nullptr != pipeline) {
        delete pipeline;
        pipeline = nullptr;
    }
}

void Render::post() {
    pipeline->queueEvent([=] {
        ++count;
        LOGI("Handle %d", count);
    });
}
