/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/BaseDrawer.h"
#include "log.h"

#define COORDS_PER_VERTEX  2
#define TEXTURE_COORDS_PER_VERTEX 2
#define COORDS_BYTE_SIZE  COORDS_PER_VERTEX * 4 * 4

BaseDrawer::BaseDrawer() {
    createVBOs();
    updateLocation(new float[8]{
            0.0f, 0.0f,//LEFT,BOTTOM
            1.0f, 0.0f,//RIGHT,BOTTOM
            0.0f, 1.0f,//LEFT,TOP
            1.0f, 1.0f//RIGHT,TOP
    }, new float[8]{
            -1.0f, -1.0f,//LEFT,BOTTOM
            1.0f, -1.0f,//RIGHT,BOTTOM
            -1.0f, 1.0f,//LEFT,TOP
            1.0f, 1.0f//RIGHT,TOP
    });
}

BaseDrawer::~BaseDrawer() {
    glDeleteBuffers(1, &vbo);
    if (GL_NONE != program)
        glDeleteProgram(program);
    delete[]position;
    delete[]texCoordinate;
//    if (GL_NONE != vao) {
//        GLES30.glDeleteVertexArrays(1, &vao);
//    }
}

void BaseDrawer::draw(GLuint texture) {
    glUseProgram(program);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, texture);
    glUniform1i(uTextureLocation, 0);
    enableVertex(aPositionLocation, aTextureCoordinateLocation);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    glDisableVertexAttribArray(aPositionLocation);
    glDisableVertexAttribArray(aTextureCoordinateLocation);
    glBindTexture(GL_TEXTURE_2D, GL_NONE);
    glUseProgram(GL_NONE);
//    glFlush();
}

GLuint BaseDrawer::createProgram(string vertex, string fragment) {
    GLuint program = glCreateProgram();
    if (program == GL_NONE) {
        LOGE("Create program failed: %d", glGetError());
    }
    GLuint vertexShader = createShader(GL_VERTEX_SHADER, vertex);
    GLuint fragmentShader = createShader(GL_FRAGMENT_SHADER, fragment);
    //附着顶点和片段着色器
    glAttachShader(program, vertexShader);
    glAttachShader(program, fragmentShader);
    //链接program
    glLinkProgram(program);
    //告诉OpenGL ES使用此program
    glUseProgram(program);
    return program;
}

/**
 * Create a shader.
 * @param type Shader type.
 * @param shader Shader source code.
 */
GLuint BaseDrawer::createShader(GLenum type, string shader) {
    GLuint shaderId = glCreateShader(type);
    if (shaderId == 0) {
        LOGE("Create Shader Failed: %d", glGetError());
        return 0;
    }
    //加载Shader代码
    const char *str = shader.c_str();
    glShaderSource(shaderId, 1, &str, 0);
    //编译Shader
    glCompileShader(shaderId);
#ifdef GL_DEBUG
    GLint status;
    glGetShaderiv(shaderId, GL_COMPILE_STATUS, &status);
    if (1 != status) {
        GLint len;
        glGetShaderiv(shaderId, GL_INFO_LOG_LENGTH, &len);
        vector<char> log(static_cast<unsigned int>(len));
        glGetShaderInfoLog(shaderId, len, nullptr, log.data());
        string str(begin(log), end(log));
        LOGE("Shader(%d) error:\n%s\nSource: %s", type, shader, str);
        glDeleteShader(shaderId);
    }
#endif
    return shaderId;
}

void BaseDrawer::enableVertex(GLuint posLoc, GLuint texLoc) {
    updateVBOs();
    if (enableVAO) {
//        enableVAO(posLoc, texLoc);
        return;
    }
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glEnableVertexAttribArray(posLoc);
    glEnableVertexAttribArray(texLoc);
    //xy
    glVertexAttribPointer(posLoc, COORDS_PER_VERTEX, GL_FLOAT, GL_FALSE, 0, 0);
    //st
    glVertexAttribPointer(texLoc, TEXTURE_COORDS_PER_VERTEX, GL_FLOAT, GL_FALSE, 0, (GLvoid *) 0);
    glBindBuffer(GL_ARRAY_BUFFER, GL_NONE);
}

void BaseDrawer::_enableVAO(GLuint posLoc, GLuint texLoc) {
//    if (GL_NONE != vao) {
//        glGenVertexArrays(1, &vao);
//        glBindVertexArray(vao);
//        glBindBuffer(GL_ARRAY_BUFFER, vbo);
//
//        glEnableVertexAttribArray(posLoc);
//        glEnableVertexAttribArray(texLoc);
//
//        glVertexAttribPointer(posLoc, COORDS_PER_VERTEX, GL_FLOAT, false, 0, 0);
//        glVertexAttribPointer(texLoc, TEXTURE_COORDS_PER_VERTEX, GL_FLOAT, false, 0,
//                              COORDS_BYTE_SIZE);
//        glBindVertexArray(GL_NONE);
//    }
//    glBindVertexArray(vao);
}

void BaseDrawer::updateVBOs() {
    if (!requestUpdateLocation) return;
    requestUpdateLocation = false;
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glBufferSubData(GL_ARRAY_BUFFER, 0, COORDS_BYTE_SIZE, position);
    glBufferSubData(GL_ARRAY_BUFFER, COORDS_BYTE_SIZE, COORDS_BYTE_SIZE, texCoordinate);
    glBindBuffer(GL_ARRAY_BUFFER, GL_NONE);
}

void BaseDrawer::createVBOs() {
    glGenBuffers(1, &vbo);
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glBufferData(GL_ARRAY_BUFFER, COORDS_BYTE_SIZE * 2, nullptr, GL_STATIC_DRAW);
    glBindBuffer(GL_ARRAY_BUFFER, GL_NONE);
}

void BaseDrawer::updateLocation(float *texCoordinate, float *position) {
    this->texCoordinate = texCoordinate;
    this->position = position;
    requestUpdateLocation = true;
}

GLint BaseDrawer::getAttribLocation(string name) {
    return glGetAttribLocation(program, name.c_str());
}

GLint BaseDrawer::getUniformLocation(string name) {
    return glGetUniformLocation(program, name.c_str());
}
