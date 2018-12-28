/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */

#ifndef HARDWAREVIDEOCODEC_RENDER_H
#define HARDWAREVIDEOCODEC_RENDER_H

#include "Unit.h"
#include "Filter.h"

class Render : public Unit {
public:
    Render();

    virtual ~Render();

    virtual void release() override;

    void checkFilter(int width, int height);

    void renderFilter(GLuint texture);

    void renderScreen();

    bool eventPrepare(Message *msg);

    bool eventFilter(Message *msg);

private:
    Filter *filter = nullptr;
};


#endif //HARDWAREVIDEOCODEC_RENDER_H
