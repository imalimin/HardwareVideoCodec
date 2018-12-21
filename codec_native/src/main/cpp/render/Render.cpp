/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <log.h>
#include "Render.h"

Render::Render() {
    handlerThread = new HandlerThread("Render");
}

Render::~Render() {
    if (nullptr != handlerThread) {
        delete handlerThread;
        handlerThread = nullptr;
    }
}

void Render::post() {
    handlerThread->sendMessage(new Message(
            count++, [](Message *msg) {
                LOGI("Handle %d", msg->what);
            })
    );
}
