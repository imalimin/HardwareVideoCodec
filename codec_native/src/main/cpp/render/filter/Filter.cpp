//
// Created by limin on 2018/12/16.
//

#include "../include/Filter.h"
#include "log.h"

Filter::Filter() {
    name = __func__;
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

bool Filter::init(int w, int h) {
    if (initted)
        return false;
    fbo = new FrameBuffer(w, h);
    initted = true;
    return true;
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
    int key = FILTER_NONE;
    for (int i = 0;; ++i) {
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