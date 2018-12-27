/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_BASETEXTURE_H
#define HARDWAREVIDEOCODEC_BASETEXTURE_H

#include "Object.h"
#include "Size.h"
#include <GLES2/gl2.h>

class FrameBuffer : public Object {
public:
    FrameBuffer(int w, int h);

    virtual ~FrameBuffer();

    virtual int width();

    virtual int height();

    GLuint getFrameTexture() {
        return id;
    }

    GLuint getFrameBuffer() {
        return fbo;
    }

private:
    Size *size = nullptr;
    GLuint id = GL_NONE;
    GLuint fbo = GL_NONE;
    GLint fmt = GL_RGBA;

    void createTexture();
};


#endif //HARDWAREVIDEOCODEC_BASETEXTURE_H
