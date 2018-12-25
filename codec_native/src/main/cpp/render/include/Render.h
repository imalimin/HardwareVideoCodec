/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "Unit.h"

#ifndef HARDWAREVIDEOCODEC_RENDER_H
#define HARDWAREVIDEOCODEC_RENDER_H

class Render : public Unit {
public:
    Render();

    virtual ~Render();

//    void post();

    bool dispatch(Message *msg) override;

private:
//    EventPipeline *pipeline = nullptr;
    int count = 0;
};


#endif //HARDWAREVIDEOCODEC_RENDER_H
