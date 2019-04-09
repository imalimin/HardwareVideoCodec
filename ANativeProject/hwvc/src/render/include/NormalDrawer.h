/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_NORMALDRAWER_H
#define HARDWAREVIDEOCODEC_NORMALDRAWER_H

#include <string>
#include "BaseDrawer.h"

using namespace std;

class NormalDrawer : public BaseDrawer {
public:
    NormalDrawer();

    NormalDrawer(string vertex, string fragment);

    virtual ~NormalDrawer();

    virtual GLuint getProgram();

private:
    string vertex;
    string fragment;
};


#endif //HARDWAREVIDEOCODEC_NORMALDRAWER_H
