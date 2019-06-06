//
// Created by mingyi.li on 2018/12/25.
//

#ifndef HARDWAREVIDEOCODEC_ANATIVEWINDOW_H
#define HARDWAREVIDEOCODEC_ANATIVEWINDOW_H

#include "../../common/include/Object.h"
#include "Egl.h"
#include "HwWindow.h"

class NativeWindow : public Object {
public:
    HwWindow *win;

    Egl *egl = nullptr;

    NativeWindow(HwWindow *win, Egl *egl);

    virtual ~NativeWindow();

};


#endif //HARDWAREVIDEOCODEC_ANATIVEWINDOW_H
