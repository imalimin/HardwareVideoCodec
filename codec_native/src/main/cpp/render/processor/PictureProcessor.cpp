//
// Created by mingyi.li on 2018/12/25.
//

#include "../include/PictureProcessor.h"
#include "../include/Render.h"
#include "ObjectBox.h"

PictureProcessor::PictureProcessor() {
    pipeline = new MainPipeline(__func__);
    pipeline->registerAnUnit(new Render());
    pipeline->registerAnUnit(new Screen());
}

PictureProcessor::~PictureProcessor() {
    if (pipeline) {
        Message *msg = new Message(EVENT_PIPELINE_RELEASE, nullptr);
        pipeline->postEvent(msg);
        delete pipeline;
        pipeline = nullptr;
    }
}

void PictureProcessor::prepare(ANativeWindow *win, int width, int height) {
    if (pipeline) {
        Message *msg = new Message(EVENT_PIPELINE_PREPARE, nullptr);
        msg->obj = new ObjectBox(win);
        msg->arg1 = width;
        msg->arg2 = height;
        pipeline->postEvent(msg);
    }
}

void PictureProcessor::show(uint8_t *rgba, int width, int height) {
    if (!pipeline) return;
    Message *msg = new Message(EVENT_PIPELINE_DRAW_SCREEN, nullptr);
    msg->obj = new ObjectBox(rgba);
    msg->arg1 = width;
    msg->arg2 = height;
    pipeline->postEvent(msg);
}
