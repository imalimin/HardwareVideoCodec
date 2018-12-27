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
}

Render::~Render() {
    LOGE("~Render");
}

void Render::release() {
    Unit::release();
    if (filter) {
        delete filter;
        filter = nullptr;
    }
}

bool Render::dispatch(Message *msg) {
    Unit::dispatch(msg);
    switch (msg->what) {
        case EVENT_COMMON_PREPARE: {
            return true;
        }
        case EVENT_COMMON_RELEASE: {
            release();
            return true;
        }
        case EVENT_RENDER_FILTER: {
            Size *size = static_cast<Size *>(msg->tyrUnBox());
            checkFilter(size->width, size->height);
            renderFilter(msg->arg1);
            renderScreen();
            delete size;
            return true;
        }
        default:
            break;
    }
    return false;
}

void Render::checkFilter(int width, int height) {
    if (!filter) {
        filter = new NormalFilter(width, height);
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
