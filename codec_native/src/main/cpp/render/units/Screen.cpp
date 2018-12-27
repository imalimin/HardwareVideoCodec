//
// Created by mingyi.li on 2018/12/25.
//

#include "../include/Screen.h"
#include "../include/NormalDrawer.h"
#include "Size.h"

Screen::Screen() {
    name = __func__;
}

Screen::~Screen() {
    LOGE("~Screen");
}

void Screen::release() {
    Unit::release();
    if (egl) {
        egl->makeCurrent();
    }
    if (drawer) {
        delete drawer;
        drawer = nullptr;
    }
    if (egl) {
        delete egl;
        egl = nullptr;
    }
    LOGE("EVENT_PIPELINE_RELEASE");
}

bool Screen::dispatch(Message *msg) {
    Unit::dispatch(msg);
    switch (msg->what) {
        case EVENT_COMMON_PREPARE: {
            width = msg->arg1;
            height = msg->arg2;
            initWindow(static_cast<ANativeWindow *>(msg->tyrUnBox()));
            return true;
        }
        case EVENT_SCREEN_DRAW: {
            Size *size = static_cast<Size *>(msg->tyrUnBox());
            egl->makeCurrent();
            setScaleType(size->width, size->height);
            draw(msg->arg1);
            return true;
        }
        case EVENT_COMMON_RELEASE: {
            release();
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
        drawer->setRotation(ROTATION_VERTICAL);
    }
}

void Screen::draw(GLuint texture) {
    glViewport(0, 0, egl->width(), egl->height());
    glClear(GL_COLOR_BUFFER_BIT);
    glClearColor(0.0, 0.0, 0.0, 0.0);
    drawer->draw(texture);
    egl->swapBuffers();
}

void Screen::setScaleType(int dw, int dh) {
    int viewWidth = egl->width();
    int viewHeight = egl->height();
    float viewScale = viewWidth / (float) viewHeight;
    float picScale = dw / (float) dh;

    int destViewWidth = viewWidth;
    int destViewHeight = viewHeight;
    if (viewScale > picScale) {
        destViewWidth = (int) (viewHeight * picScale);
    } else {
        destViewHeight = (int) (viewWidth / picScale);
    }
    float left = -destViewWidth / (float) viewWidth;
    float right = -left;
    float bottom = -destViewHeight / (float) viewHeight;
    float top = -bottom;

    float *texCoordinate = new float[8]{
            0.0f, 0.0f,//LEFT,BOTTOM
            1.0f, 0.0f,//RIGHT,BOTTOM
            0.0f, 1.0f,//LEFT,TOP
            1.0f, 1.0f//RIGHT,TOP
    };
    float *position = new float[8]{
            left, bottom, //LEFT,BOTTOM
            right, bottom, //RIGHT,BOTTOM
            left, top, //LEFT,TOP
            right, top,//RIGHT,TOP
    };

    drawer->updateLocation(texCoordinate, position);
    delete[]texCoordinate;
    delete[]position;
}