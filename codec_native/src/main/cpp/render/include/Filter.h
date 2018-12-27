//
// Created by limin on 2018/12/16.
//

#ifndef HARDWAREVIDEOCODEC_FILTER_H
#define HARDWAREVIDEOCODEC_FILTER_H

#include <string>
#include "Object.h"
#include "FrameBuffer.h"
#include "BaseDrawer.h"

using namespace std;

class Filter : public Object {
public:
    string name;

    Filter(int w, int h);

    virtual ~Filter();

    virtual void draw(GLuint texture);

    virtual void bindResources();

    FrameBuffer *getFrameBuffer() {
        return fbo;
    }

protected:
    BaseDrawer *drawer = nullptr;
private:
    FrameBuffer *fbo = nullptr;

};


#endif //HARDWAREVIDEOCODEC_FILTER_H
