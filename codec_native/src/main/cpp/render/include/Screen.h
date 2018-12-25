//
// Created by mingyi.li on 2018/12/25.
//

#ifndef HARDWAREVIDEOCODEC_SCREEN_H
#define HARDWAREVIDEOCODEC_SCREEN_H

#include "Unit.h"
#include "Egl.h"
#include "BaseDrawer.h"

class Screen : public Unit {
public:
    Screen();

    virtual ~Screen();

    bool dispatch(Message *msg) override;

private:
    Egl *egl = nullptr;
    BaseDrawer *drawer;
    int width = 0;
    int height = 0;

    void initWindow(ANativeWindow *win);

    void draw(GLuint texture);
};


#endif //HARDWAREVIDEOCODEC_SCREEN_H
