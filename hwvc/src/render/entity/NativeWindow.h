//
// Created by mingyi.li on 2018/12/25.
//

#ifndef HARDWAREVIDEOCODEC_ANATIVEWINDOW_H
#define HARDWAREVIDEOCODEC_ANATIVEWINDOW_H

#include "Object.h"
#include "../include/Egl.h"

class NativeWindow : public Object {
public:
    ANativeWindow *win;

    Egl *egl = nullptr;

    NativeWindow(ANativeWindow *win, Egl *egl);

    virtual ~NativeWindow();

};


#endif //HARDWAREVIDEOCODEC_ANATIVEWINDOW_H
