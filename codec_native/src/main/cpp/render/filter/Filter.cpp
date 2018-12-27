//
// Created by limin on 2018/12/16.
//

#include "../include/Filter.h"

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
    drawer->draw(texture);
    glBindFramebuffer(GL_FRAMEBUFFER, GL_NONE);

}