//
// Created by mingyi.li on 2018/12/25.
//

#include "../include/Screen.h"
#include "../include/NormalDrawer.h"
#include "Size.h"
#include "Logcat.h"

Screen::Screen() {
    name = __FUNCTION__;
    registerEvent(EVENT_COMMON_PREPARE, reinterpret_cast<EventFunc>(&Screen::eventPrepare));
    registerEvent(EVENT_SCREEN_DRAW, reinterpret_cast<EventFunc>(&Screen::eventDraw));
}

Screen::Screen(HandlerThread *handlerThread) : Unit(handlerThread) {
    name = __FUNCTION__;
    registerEvent(EVENT_COMMON_PREPARE, reinterpret_cast<EventFunc>(&Screen::eventPrepare));
    registerEvent(EVENT_SCREEN_DRAW, reinterpret_cast<EventFunc>(&Screen::eventDraw));
}

Screen::~Screen() {
}

bool Screen::eventRelease(Message *msg) {
    Logcat::i("HWVC", "Screen::eventRelease");
    post([this] {
//        if (egl) {
//            egl->makeCurrent();
//        }
        if (drawer) {
            delete drawer;
            drawer = nullptr;
        }
//        if (egl) {
//            delete egl;
//            egl = nullptr;
//        }
    });
    return true;
}

bool Screen::eventPrepare(Message *msg) {
    Logcat::i("HWVC", "%s", __func__);
    width = msg->arg1;
    height = msg->arg2;
    NativeWindow *nw = static_cast<NativeWindow *>(msg->tyrUnBox());
    post([this, nw] {
        initWindow(nw);
    });
    return true;
}

bool Screen::eventDraw(Message *msg) {
    Logcat::i("HWVC", "%s", __func__);
    Size *size = static_cast<Size *>(msg->tyrUnBox());
    GLuint tex = msg->arg1;
    post([this, size, tex] {
        Logcat::i("HWVC", "eventDraw makeCurrent a");
//        egl->makeCurrent();
        Logcat::i("HWVC", "eventDraw makeCurrent b");
        setScaleType(size->width, size->height);
        draw(tex);
        delete size;
    });
    return true;
}

void Screen::initWindow(NativeWindow *nw) {
//    if (!egl) {
//        if (nw->egl) {
//            Logcat::i("HWVC", "Screen::init EGL with context");
//            egl = new Egl(nw->egl, nw->win);
//        } else {
//            Logcat::i("HWVC", "Screen::init EGL");
//            egl = new Egl(nw->win);
//            nw->egl = egl;
//        }
//        Logcat::i("HWVC", "%s makeCurrent a", __FUNCTION__);
//        //TODO 这里不知道为什么会阻塞
//        egl->makeCurrent();
//        Logcat::i("HWVC", "%s makeCurrent b", __FUNCTION__);
//        //TODO 这里不知道为什么会阻塞
//        drawer = new NormalDrawer();
//        drawer->setRotation(ROTATION_VERTICAL);
//        Logcat::i("HWVC", "Screen::initWindow %d x %d", egl->width(), egl->height());
//    }
    drawer = new NormalDrawer();
    drawer->setRotation(ROTATION_VERTICAL);
}

void Screen::draw(GLuint texture) {
    Logcat::i("HWVC", "%s", __func__);
//    string glslVersion = (const char *) glGetString(GL_SHADING_LANGUAGE_VERSION);
//    LOGE("version: %s", glslVersion.c_str());
    glViewport(0, 0, 1440, 2872);
    glClear(GL_COLOR_BUFFER_BIT);
    glClearColor(0.0, 0.0, 0.0, 0.0);
    //TODO 这里不知道为什么会阻塞
    drawer->draw(texture);
    //TODO 这里不知道为什么会阻塞
//    egl->swapBuffers();
}

void Screen::setScaleType(int dw, int dh) {
    int viewWidth = 1440;
    int viewHeight = 2872;
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