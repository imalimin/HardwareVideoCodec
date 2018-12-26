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

#define SHADER(...) #__VA_ARGS__

using namespace std;

class BaseDrawer : public Object {
public:
    BaseDrawer();

    virtual ~BaseDrawer();

    virtual void draw(GLuint texture);

    virtual GLuint createProgram(string vertex, string fragment);

    virtual GLuint getProgram() = 0;

    GLint getAttribLocation(string name);

    GLint getUniformLocation(string name);

    void updateLocation(float *texCoordinate, float *position);

protected:
    bool enableVAO = false;
    GLuint program = GL_NONE;
    GLint uTextureLocation = GL_NONE;
    GLuint aPositionLocation = GL_NONE;
    GLuint aTextureCoordinateLocation = GL_NONE;
    GLvoid *position = new float[8];
    GLvoid *texCoordinate = new float[8];
    bool requestUpdateLocation = false;
    GLuint vbo = GL_NONE;
    GLuint vao = GL_NONE;

    GLuint createShader(GLenum type, string shader);

    void createVBOs();

    void enableVertex(GLuint posLoc, GLuint texLoc);

    void _enableVAO(GLuint posLoc, GLuint texLoc);

    void updateVBOs();
};


#endif //HARDWAREVIDEOCODEC_BASEDRAWER_H
