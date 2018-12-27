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

//每个点占多少字节
#define SIZE_OF_VERTEX  4
//一个顶点坐标包含几个点
#define COUNT_PER_VERTEX  2
//所有顶点坐标总共多少字节
#define VERTEX_BYTE_SIZE  COUNT_PER_VERTEX * SIZE_OF_VERTEX * 4

#define ROTATION_VERTICAL 1
#define ROTATION_HORIZONTAL 2
#define ROTATION_CLOCKWISE_90 3
#define ROTATION_CLOCKWISE_180 4
#define ROTATION_CLOCKWISE_270 5

#define SHADER(...) #__VA_ARGS__

using namespace std;

class BaseDrawer : public Object {
public:
    BaseDrawer();

    virtual ~BaseDrawer();

    virtual void init();

    virtual void draw(GLuint texture);

    virtual GLuint createProgram(string vertex, string fragment);

    virtual GLuint getProgram() = 0;

    void updateLocation(float *texCoordinate, float *position);

    /**
    * 通过改变顶点坐标，达到旋转到目的
    */
    void setRotation(int rotation);

    void useProgram();

    GLint getAttribLocation(string name);

    GLint getUniformLocation(string name);

    void setUniform1f(GLint location, float value);

protected:
    bool enableVAO = false;
    GLuint program = GL_NONE;
    GLint uTextureLocation = GL_NONE;
    GLuint aPositionLocation = GL_NONE;
    GLuint aTextureCoordinateLocation = GL_NONE;
    float *position = new float[8];
    float *texCoordinate = new float[8];
    bool requestUpdateLocation = false;
    GLuint vbo = GL_NONE;
    GLuint vao = GL_NONE;
    int rotation = 0;

    GLuint createShader(GLenum type, string shader);

    void createVBOs();

    void enableVertex(GLuint posLoc, GLuint texLoc);

    void _enableVAO(GLuint posLoc, GLuint texLoc);

    void updateVBOs();

    /**
     * 根据rotation旋转顶点坐标
     */
    void rotateVertex(int rotation);
};


#endif //HARDWAREVIDEOCODEC_BASEDRAWER_H
