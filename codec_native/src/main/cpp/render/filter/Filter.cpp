//
// Created by limin on 2018/12/16.
//

#include "../include/Filter.h"
#include "log.h"

Filter::Filter(int w, int h) {
    name = __func__;
    fbo = new FrameBuffer(w, h);
}

Filter::~Filter() {
    if (fbo) {
        delete fbo;
        fbo = nullptr;
    }
    if (fbo) {
        delete drawer;
        drawer = nullptr;
    }
}

void Filter::draw(GLuint texture) {
    if (!fbo || !drawer)
        return;
    glBindFramebuffer(GL_FRAMEBUFFER, fbo->getFrameBuffer());
    drawer->useProgram();
    bindResources();
    drawer->draw(texture);
    glBindFramebuffer(GL_FRAMEBUFFER, GL_NONE);
}

void Filter::bindResources() {

}

void Filter::setParams(int *params) {
    LOGE("%s", __func__);
    if (nullptr == params) return;
    int size = sizeof(params) / sizeof(int);
    LOGE("Filter::setParams size=%d", size);
    if (0 == size || 1 != size % 2) return;
    int key = FILTER_NONE;
    for (int i = 0; i < size; ++i) {
        if (0 == i % 2) {
            key = params[i];
            if (FILTER_NONE == params[i]) return;
        } else {
            setParam(key, params[i]);
        }

    }
}

void Filter::setParam(int key, int value) {

}