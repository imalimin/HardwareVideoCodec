//
// Created by limin on 2019/6/15.
//

#include "../include/HwSourcesAllocator.h"

HwSources::HwSources(HwSourcesAllocator *allocator) : Object() {
    this->allocator = allocator;
}

HwSources::~HwSources() {

}

void HwSources::recycle() {
    if (allocator) {
        HwSources *entity = this;
        allocator->unRef(&entity);
    }
}