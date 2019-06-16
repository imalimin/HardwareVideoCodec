//
// Created by limin on 2019/6/15.
//

#ifndef HARDWAREVIDEOCODEC_HWSOURCESALLOCATOR_H
#define HARDWAREVIDEOCODEC_HWSOURCESALLOCATOR_H

#include "Object.h"

class HwSourcesAllocator;

class HwSources : virtual public Object {
public:
    HwSources(HwSourcesAllocator *allocator);

    virtual ~HwSources();

    virtual void recycle();

protected:
    HwSourcesAllocator *allocator = nullptr;
};

class HwSourcesAllocator : public Object {
public:
    HwSourcesAllocator();

    virtual ~HwSourcesAllocator();

    virtual void unRef(HwSources **entity) = 0;

};


#endif //HARDWAREVIDEOCODEC_HWSOURCESALLOCATOR_H
