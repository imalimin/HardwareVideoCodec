/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "HandlerThread.h"

#ifndef HARDWAREVIDEOCODEC_RENDER_H
#define HARDWAREVIDEOCODEC_RENDER_H

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
