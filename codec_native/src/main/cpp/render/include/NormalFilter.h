//
// Created by mingyi.li on 2018/12/27.
//

#ifndef HARDWAREVIDEOCODEC_NORMALFILTER_H
#define HARDWAREVIDEOCODEC_NORMALFILTER_H

#include "Filter.h"

class NormalFilter : public Filter {
public:
    NormalFilter();

    ~NormalFilter();

    void init(int w, int h) override;
};


#endif //HARDWAREVIDEOCODEC_NORMALFILTER_H
