//
// Created by limin on 2019/6/15.
//

#include "../include/HwSourcesAllocator.h"
#include "../include/Logcat.h"

HwSources::HwSources(HwSourcesAllocator *allocator) : Object() {
    this->allocator = allocator;
}

HwSources::~HwSources() {
    allocator = nullptr;
}

void HwSources::recycle() {
    if (!isDetach()) {
        HwSources *entity = this;
        if (!allocator->recycle(&entity)) {
            Logcat::e("HWVC", "HwSources recycle failed. Is it(%p) managed by allocator(%p)",
                      this,
                      allocator);
        }
    }
}

bool HwSources::isDetach() {
    return nullptr == allocator;
}