//
// Created by mingyi.li on 2018/12/25.
//

#include "../include/PictureProcessor.h"
#include "../include/Render.h"
#include "../include/Image.h"
#include "ObjectBox.h"

PictureProcessor::PictureProcessor() {
    pipeline = new MainPipeline(__func__);
    pipeline->registerAnUnit(new Render());
    pipeline->registerAnUnit(new Screen());
    pipeline->registerAnUnit(new Image());
}

PictureProcessor::~PictureProcessor() {
    if (pipeline) {
        Message *msg = new Message(EVENT_COMMON_RELEASE, nullptr);
        pipeline->postEvent(msg);
        delete pipeline;
        pipeline = nullptr;
    }
}

void PictureProcessor::prepare(ANativeWindow *win, int width, int height) {
    if (pipeline) {
        Message *msg = new Message(EVENT_COMMON_PREPARE, nullptr);
        msg->obj = new ObjectBox(win);
        msg->arg1 = width;
        msg->arg2 = height;
        pipeline->postEvent(msg);
    }
}

void PictureProcessor::show(char *path) {
    if (!pipeline) return;
    Message *msg = new Message(EVENT_IMAGE_SHOW, nullptr);
    msg->obj = new ObjectBox(path);
    pipeline->postEvent(msg);
}
