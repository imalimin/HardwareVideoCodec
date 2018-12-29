/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include <log.h>
#include "../include/Render.h"
#include "../include/BeautyV4Filter.h"
#include "../include/ObjectBox.h"

Render::Render() {
    name = __func__;
    filter = new BeautyV4Filter();
    registerEvent(EVENT_COMMON_PREPARE, reinterpret_cast<EventFunc>(&Render::eventPrepare));
    registerEvent(EVENT_RENDER_FILTER, reinterpret_cast<EventFunc>(&Render::eventFilter));
    registerEvent(EVENT_RENDER_FILTER_PARAMS,
                  reinterpret_cast<EventFunc>(&Render::eventFilterParams));
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
        filter->init(width, height);
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

void Render::setFilterParams(int *params) {
    if (filter) {
        filter->setParams(params);
    }
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

bool Render::eventFilterParams(Message *msg) {
    int *params = static_cast<int *>(msg->tyrUnBox());
    setFilterParams(params);
    return true;
}