//
// Created by limin on 2018/12/16.
//

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
