//
// Created by mingyi.li on 2018/12/25.
//

#include "../include/PictureProcessor.h"
#include "../include/Render.h"
#include "../entity/NativeWindow.h"

PictureProcessor::PictureProcessor() {
    pipeline = new MainPipeline(__func__);
    pipeline->registerAnUnit(new Render());
    pipeline->registerAnUnit(new Screen());
}

PictureProcessor::~PictureProcessor() {
    if (pipeline) {
        delete pipeline;
        pipeline = nullptr;
    }
}

void PictureProcessor::prepare(ANativeWindow *win) {
    if (pipeline) {
        Message *msg = new Message(EVENT_PIPELINE_PREPARE, nullptr);
        msg->obj = new NativeWindow(win);
        pipeline->postEvent(msg);
    }
}
