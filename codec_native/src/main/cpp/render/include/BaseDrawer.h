/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#ifndef HARDWAREVIDEOCODEC_BASEDRAWER_H
#define HARDWAREVIDEOCODEC_BASEDRAWER_H

#include "Object.h"
#include <string>
#include <GLES2/gl2.h>

using namespace std;

class BaseDrawer : public Object {
public:
    BaseDrawer();

    virtual ~BaseDrawer();

    virtual void draw(GLuint texture);

    virtual void createProgram(string vertex, string fragment);

private:
    bool enableVAO = false;
    GLuint program = GL_NONE;

    GLuint createShader(GLenum type, string shader);

};


#endif //HARDWAREVIDEOCODEC_BASEDRAWER_H
