/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/BaseDrawer.h"
#include "log.h"
#include <vector>

BaseDrawer::BaseDrawer() {

}

BaseDrawer::~BaseDrawer() {

}

void BaseDrawer::draw(GLuint texture) {

}

void BaseDrawer::createProgram(string vertex, string fragment) {
    program = glCreateProgram();
    if (program == GL_NONE) {
        LOGE("Create program failed: %d" + glGetError());
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
    glShaderSource(shaderId, 1, reinterpret_cast<const GLchar **>(shader.c_str()), 0);
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
