//
// Created by limin on 2018/12/26.
//

#ifndef HARDWAREVIDEOCODEC_OBJECTBOX_H
#define HARDWAREVIDEOCODEC_OBJECTBOX_H

#include "Object.h"

class ObjectBox : public Object {
public:
    void *ptr;

    ObjectBox(void *ptr);

    virtual ~ObjectBox();
};


#endif //HARDWAREVIDEOCODEC_OBJECTBOX_H
