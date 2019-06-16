//
// Created by limin on 2019/6/15.
//

#include "../include/HwSourcesAllocator.h"

HwSources::HwSources(HwSourcesAllocator *allocator) : Object() {
    this->allocator = allocator;
}

HwSources::~HwSources() {
    allocator = nullptr;
}

void HwSources::recycle() {
    if (!isDetach()) {
        HwSources *entity = this;
        allocator->unRef(&entity);
    }
}

bool HwSources::isDetach() {
    return nullptr == allocator;
}