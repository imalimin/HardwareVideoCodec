/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/BaseDrawer.h"
#include "log.h"

BaseDrawer::BaseDrawer() {
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

void BaseDrawer::init() {
    createVBOs();

    float *texCoordinate = new float[8]{
            0.0f, 0.0f,//LEFT,BOTTOM
            1.0f, 0.0f,//RIGHT,BOTTOM
            0.0f, 1.0f,//LEFT,TOP
            1.0f, 1.0f//RIGHT,TOP
    };
    float *position = new float[8]{
            -1.0f, -1.0f,//LEFT,BOTTOM
            1.0f, -1.0f,//RIGHT,BOTTOM
            -1.0f, 1.0f,//LEFT,TOP
            1.0f, 1.0f//RIGHT,TOP
    };
    updateLocation(texCoordinate, position);
    delete[]texCoordinate;
    delete[]position;
    program = getProgram();
    aPositionLocation = static_cast<GLuint>(getAttribLocation("aPosition"));
    uTextureLocation = getUniformLocation("uTexture");
    aTextureCoordinateLocation = static_cast<GLuint>(getAttribLocation("aTextureCoord"));
}

void BaseDrawer::draw(GLuint texture) {
    useProgram();
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, texture);
    glUniform1i(uTextureLocation, 0);
    enableVertex(aPositionLocation, aTextureCoordinateLocation);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    glDisableVertexAttribArray(aPositionLocation);
    glDisableVertexAttribArray(aTextureCoordinateLocation);
    glBindTexture(GL_TEXTURE_2D, GL_NONE);
    glUseProgram(GL_NONE);
    programUsed = false;
//    glFlush();
}

void BaseDrawer::useProgram() {
    if (programUsed)
        return;
    glUseProgram(program);
    programUsed = true;
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
        LOGE("Shader(%d) error:\n%s\nSource: %s", type, shader.c_str(), str.c_str());
        glDeleteShader(shaderId);
    }
#endif
    return shaderId;
}

void BaseDrawer::enableVertex(GLuint posLoc, GLuint texLoc) {
    updateVBOs();
    if (enableVAO) {
        _enableVAO(posLoc, texLoc);
        return;
    }
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glEnableVertexAttribArray(posLoc);
    glEnableVertexAttribArray(texLoc);
    //xy
    glVertexAttribPointer(posLoc, COUNT_PER_VERTEX, GL_FLOAT, GL_FALSE, 0, 0);
    //st
    glVertexAttribPointer(texLoc, COUNT_PER_VERTEX, GL_FLOAT, GL_FALSE, 0,
                          reinterpret_cast<const void *>(VERTEX_BYTE_SIZE));
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
    rotateVertex(rotation);
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glBufferSubData(GL_ARRAY_BUFFER, 0, VERTEX_BYTE_SIZE, position);
    glBufferSubData(GL_ARRAY_BUFFER, VERTEX_BYTE_SIZE, VERTEX_BYTE_SIZE, texCoordinate);
    glBindBuffer(GL_ARRAY_BUFFER, GL_NONE);
}

void BaseDrawer::createVBOs() {
    glGenBuffers(1, &vbo);
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glBufferData(GL_ARRAY_BUFFER, VERTEX_BYTE_SIZE * 2, nullptr, GL_STATIC_DRAW);
    glBindBuffer(GL_ARRAY_BUFFER, GL_NONE);
}

void BaseDrawer::updateLocation(float *texCoordinate, float *position) {
    memcpy(this->texCoordinate, texCoordinate, VERTEX_BYTE_SIZE);
    memcpy(this->position, position, VERTEX_BYTE_SIZE);
    requestUpdateLocation = true;
}

GLint BaseDrawer::getAttribLocation(string name) {
    return glGetAttribLocation(program, name.c_str());
}

GLint BaseDrawer::getUniformLocation(string name) {
    return glGetUniformLocation(program, name.c_str());
}

void BaseDrawer::setRotation(int rotation) {
    this->rotation = rotation;
    this->requestUpdateLocation = true;
}

void BaseDrawer::rotateVertex(int rotation) {
    int size = 0;
    switch (rotation) {
        case ROTATION_VERTICAL: {
            size = 4 * 4;
            float *tmp = new float[4];
            memcpy(tmp, position, size);
            memcpy(position, &position[4], size);
            memcpy(&position[4], tmp, size);
            delete[]tmp;
            break;
        }
        case ROTATION_HORIZONTAL: {
            size = 2 * 4;
            int offset = 0;
            float *tmp = new float[2];
            memcpy(tmp, &position[offset], size);
            memcpy(&position[offset], &position[offset + 2], size);
            memcpy(&position[offset + 2], tmp, size);
            offset = 4;
            memcpy(tmp, &position[offset], size);
            memcpy(&position[offset], &position[offset + 2], size);
            memcpy(&position[offset + 2], tmp, size);
            delete[]tmp;
            break;
        }
        default:
            break;
    }
}

void BaseDrawer::setUniform1f(GLint location, float value) {
    glUniform1f(location, value);
}
