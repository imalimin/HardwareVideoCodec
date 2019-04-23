/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "Logcat.h"
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
    Logcat::i("HWVC", "Render::eventRelease");
    post([this] {
        if (filter) {
            delete filter;
            Logcat::i("HWVC", "Render::eventRelease filter");
            filter = nullptr;
        }
    });
    return true;
}

void Render::checkFilter(int width, int height) {
    if (filter) {
        bool ret = filter->init(width, height);
    }
}

void Render::renderFilter(GLuint texture) {
    Logcat::i("HWVC", "Render::renderFilter");
    filter->draw(texture);
}

void Render::renderScreen() {
    Logcat::i("HWVC", "Render::renderScreen");
    Message *msg = new Message(EVENT_SCREEN_DRAW, nullptr);
    msg->obj = new ObjectBox(new Size(filter->getFrameBuffer()->width(),
                                      filter->getFrameBuffer()->height()));
    msg->arg1 = filter->getFrameBuffer()->getFrameTexture();
    postEvent(msg);
}

bool Render::eventPrepare(Message *msg) {
    Logcat::i("HWVC", "Render::eventPrepare");
    return true;
}

bool Render::eventFilter(Message *msg) {
    Logcat::i("HWVC", "Render::eventFilter");
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
    Logcat::i("HWVC", "Render::eventSetFilter");
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