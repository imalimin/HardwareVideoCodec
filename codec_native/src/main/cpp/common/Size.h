/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "Object.h"

#ifndef HARDWAREVIDEOCODEC_SIZE_H
#define HARDWAREVIDEOCODEC_SIZE_H

class Size : public Object {
public:
    int width;
    int height;

    Size();

    Size(int w, int h);

    virtual ~Size();
};


#endif //HARDWAREVIDEOCODEC_SIZE_H
