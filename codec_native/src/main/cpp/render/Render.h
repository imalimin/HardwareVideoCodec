//
// Created by limin on 2018/12/16.
//

#ifndef HARDWAREVIDEOCODEC_RENDER_H
#define HARDWAREVIDEOCODEC_RENDER_H


#include "HandlerThread.h"

class Render {
public:
    Render();

    ~Render();

    void post();

private:
    HandlerThread *handlerThread = nullptr;
    int count = 0;
};


#endif //HARDWAREVIDEOCODEC_RENDER_H
