//
// Created by mingyi.li on 2018/12/25.
//

#include "../include/Screen.h"
#include "../include/NormalDrawer.h"

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
    if (texture) {
        glDeleteTextures(1, &texture);
        texture = GL_NONE;
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
            uint8_t *rgba = static_cast<uint8_t *>(msg->tyrUnBox());
            int width = msg->arg1;
            int height = msg->arg2;
            egl->makeCurrent();
            setScaleType(width, height);
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