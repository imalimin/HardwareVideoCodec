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
    name = __FUNCTION__;
    filter = new NormalFilter();
    registerEvent(EVENT_COMMON_PREPARE, reinterpret_cast<EventFunc>(&Render::eventPrepare));
    registerEvent(EVENT_RENDER_FILTER, reinterpret_cast<EventFunc>(&Render::eventFilter));
    registerEvent(EVENT_RENDER_SET_FILTER, reinterpret_cast<EventFunc>(&Render::eventSetFilter));
}

Render::Render(HandlerThread *handlerThread) : Unit(handlerThread) {
    name = __FUNCTION__;
    filter = new NormalFilter();
    registerEvent(EVENT_COMMON_PREPARE, reinterpret_cast<EventFunc>(&Render::eventPrepare));
    registerEvent(EVENT_RENDER_FILTER, reinterpret_cast<EventFunc>(&Render::eventFilter));
    registerEvent(EVENT_RENDER_SET_FILTER, reinterpret_cast<EventFunc>(&Render::eventSetFilter));
}

Render::~Render() {
}

bool Render::eventRelease(Message *msg) {
    LOGI("Render::eventRelease");
    post([this] {
        if (filter) {
            delete filter;
            LOGI("Render::eventRelease filter");
            filter = nullptr;
        }
    });
    return true;
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
    LOGI("%s", __func__);
    filter->draw(texture);
}

void Render::renderScreen() {
    LOGI("%s", __func__);
    Message *msg = new Message(EVENT_SCREEN_DRAW, nullptr);
    msg->obj = new ObjectBox(new Size(filter->getFrameBuffer()->width(),
                                      filter->getFrameBuffer()->height()));
    msg->arg1 = filter->getFrameBuffer()->getFrameTexture();
    postEvent(msg);
}

bool Render::eventPrepare(Message *msg) {
    LOGI("%s", __func__);
    return true;
}

bool Render::eventFilter(Message *msg) {
    LOGI("%s", __func__);
    Size *size = static_cast<Size *>(msg->tyrUnBox());
    GLuint tex = msg->arg1;
    post([this, size, tex] {
        checkFilter(size->width, size->height);
        glViewport(0, 0, size->width, size->height);
        renderFilter(tex);
        renderScreen();
        delete size;
    });
    return true;
}

bool Render::eventSetFilter(Message *msg) {
    LOGI("%s", __func__);
    Filter *newFilter = static_cast<Filter *>(msg->tyrUnBox());
    post([this, newFilter] {
        if (filter) {
            delete filter;
            filter = nullptr;
        }
        filter = newFilter;
    });
    return true;
}