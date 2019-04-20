//
// Created by mingyi.li on 2018/12/25.
//

#ifndef HARDWAREVIDEOCODEC_SCREEN_H
#define HARDWAREVIDEOCODEC_SCREEN_H

#include "Unit.h"
#include "Egl.h"
#include "BaseDrawer.h"
#include "../entity/NativeWindow.h"

class Screen : public Unit {
public:
    Screen();

    Screen(HandlerThread *handlerThread);

    virtual ~Screen();

    bool eventRelease(Message *msg) override;

    bool eventPrepare(Message *msg);

    bool eventDraw(Message *msg);

private:
    Egl *egl = nullptr;
    BaseDrawer *drawer = nullptr;
    int width = 0;
    int height = 0;

    void initWindow(NativeWindow *nw);

    void draw(GLuint texture);

    void setScaleType(int dw, int dh);
};


#endif //HARDWAREVIDEOCODEC_SCREEN_H
