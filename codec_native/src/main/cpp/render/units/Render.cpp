/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <log.h>
#include "../include/Render.h"
#include "../include/NormalFilter.h"
#include "../include/ObjectBox.h"

Render::Render() {
    name = __func__;
    filter = new NormalFilter();
    registerEvent(EVENT_COMMON_PREPARE, reinterpret_cast<EventFunc>(&Render::eventPrepare));
    registerEvent(EVENT_RENDER_FILTER, reinterpret_cast<EventFunc>(&Render::eventFilter));
    registerEvent(EVENT_RENDER_SET_FILTER, reinterpret_cast<EventFunc>(&Render::eventSetFilter));
}

Render::~Render() {
    release();
    LOGI("Render::~Render");
}

void Render::release() {
    Unit::release();
    LOGI("Render::release");
    if (filter) {
        delete filter;
        filter = nullptr;
    }
}

void Render::checkFilter(int width, int height) {
    if (filter) {
        bool ret = filter->init(width, height);
        if (ret) {
            LOGI("Init filter");
        }
    }
}

void Render::renderFilter(GLuint texture) {
    filter->draw(texture);
}

void Render::renderScreen() {
    Message *msg = new Message(EVENT_SCREEN_DRAW, nullptr);
    msg->obj = new ObjectBox(new Size(filter->getFrameBuffer()->width(),
                                      filter->getFrameBuffer()->height()));
    msg->arg1 = filter->getFrameBuffer()->getFrameTexture();
    postEvent(msg);
}

bool Render::eventPrepare(Message *msg) {
    return true;
}

bool Render::eventFilter(Message *msg) {
    Size *size = static_cast<Size *>(msg->tyrUnBox());
    checkFilter(size->width, size->height);
    glViewport(0, 0, size->width, size->height);
    renderFilter(msg->arg1);
    renderScreen();
    delete size;
    return true;
}

bool Render::eventSetFilter(Message *msg) {
    if (filter) {
        delete filter;
        filter = nullptr;
    }
    filter = static_cast<Filter *>(msg->tyrUnBox());
    return true;
}