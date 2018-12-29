//
// Created by limin on 2018/12/26.
//

#include "../include/ObjectBox.h"

ObjectBox::ObjectBox(void *ptr) {
    this->ptr = ptr;
}

ObjectBox::~ObjectBox() {
    this->ptr = nullptr;
}
