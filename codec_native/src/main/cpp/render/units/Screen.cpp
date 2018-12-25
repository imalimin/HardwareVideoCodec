//
// Created by mingyi.li on 2018/12/25.
//

#include "../include/Screen.h"
#include "../include/NormalDrawer.h"
#include "ObjectBox.h"

Screen::Screen() {
    name = __func__;
}

Screen::~Screen() {
    if (drawer) {
        delete drawer;
        drawer = nullptr;
    }
    if (egl) {
        delete egl;
        egl = nullptr;
    }
}

bool Screen::dispatch(Message *msg) {
    switch (msg->what) {
        case EVENT_PIPELINE_PREPARE: {
            ObjectBox *nw = dynamic_cast<ObjectBox *>(msg->obj);
            width = msg->arg1;
            height = msg->arg2;
            initWindow(static_cast<ANativeWindow *>(nw->ptr));
            delete nw;
            return true;
        }
        case EVENT_PIPELINE_DRAW_SCREEN: {
            ObjectBox *ob = dynamic_cast<ObjectBox *>(msg->obj);
            uint8_t *rgba = static_cast<uint8_t *>(ob->ptr);
            int width = msg->arg1;
            int height = msg->arg2;
            GLuint texture;
            glGenTextures(1, &texture);
            glBindTexture(GL_TEXTURE_2D, texture);
            glTexParameterf(GL_TEXTURE_2D,
                            GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameterf(GL_TEXTURE_2D,
                            GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameterf(GL_TEXTURE_2D,
                            GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameterf(GL_TEXTURE_2D,
                            GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE,
                         rgba);
            glBindTexture(GL_TEXTURE_2D, GL_NONE);
            draw(texture);
            return true;
        }
        default:
            break;
    }
    return false;
}

void Screen::initWindow(ANativeWindow *win) {
    if (!egl) {
        egl = new Egl(win);
        egl->makeCurrent();
        drawer = new NormalDrawer();
    }
}

void Screen::draw(GLuint texture) {
    egl->makeCurrent();
    LOGE("%d x %d, %d x %d", width, height, egl->width(), egl->height());
    glViewport(0, 0, width, height);
    glClear(GL_COLOR_BUFFER_BIT);
    glClearColor(0.0, 0.0, 0.0, 0.0);
    drawer->draw(texture);
    egl->swapBuffers();
}
