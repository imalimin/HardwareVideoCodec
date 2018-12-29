/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_PINKFILTER_H
#define HARDWAREVIDEOCODEC_PINKFILTER_H

#include "BaseMultipleSamplerFilter.h"

class PinkFilter : public BaseMultipleSamplerFilter {
public:
    PinkFilter(string *names, string *samplers, int size);

    ~PinkFilter();

    bool init(int w, int h) override;

    BaseDrawer *getDrawer() override;
};


#endif //HARDWAREVIDEOCODEC_PINKFILTER_H
