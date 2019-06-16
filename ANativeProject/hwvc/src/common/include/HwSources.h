//
// Created by limin on 2019/6/15.
//

#ifndef HARDWAREVIDEOCODEC_HWSOURCES_H
#define HARDWAREVIDEOCODEC_HWSOURCES_H

#include "Object.h"
#include "HwSourcesAllocator.h"

class HwSources : public Object {
public:
    HwSources(HwSourcesAllocator *allocator);

    virtual ~HwSources();

    virtual void recycle();

private:
    HwSourcesAllocator *allocator = nullptr;
};


#endif //HARDWAREVIDEOCODEC_HWSOURCES_H
