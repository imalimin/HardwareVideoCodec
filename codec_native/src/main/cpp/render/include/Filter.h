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

const int FILTER_NONE = 0;

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

    virtual void setParams(int *params);

    virtual void setParam(int key, int value);

protected:
    BaseDrawer *drawer = nullptr;
private:
    FrameBuffer *fbo = nullptr;

};


#endif //HARDWAREVIDEOCODEC_FILTER_H
