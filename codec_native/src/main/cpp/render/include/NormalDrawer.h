/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_NORMALDRAWER_H
#define HARDWAREVIDEOCODEC_NORMALDRAWER_H

#include "BaseDrawer.h"

class NormalDrawer : public BaseDrawer {
public:
    NormalDrawer();

    virtual ~NormalDrawer();

    virtual GLuint getProgram();

};


#endif //HARDWAREVIDEOCODEC_NORMALDRAWER_H
