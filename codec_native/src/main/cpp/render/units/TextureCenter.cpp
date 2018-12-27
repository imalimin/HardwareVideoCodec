//
// Created by mingyi.li on 2018/12/27.
//

#include "../include/TextureCenter.h"
#include "ObjectBox.h"
#include "log.h"

TextureCenter::TextureCenter() {
}

TextureCenter::~TextureCenter() {
    clear();
}

GLuint TextureCenter::alloc() {
    GLuint texture = GL_NONE;
    glGenTextures(1, &texture);
    if (GL_NONE == texture)
        return GL_NONE;
    textures.push_back(texture);
    glBindTexture(GL_TEXTURE_2D, texture);
    glTexParameterf(GL_TEXTURE_2D,
                    GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D,
                    GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D,
                    GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameterf(GL_TEXTURE_2D,
                    GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glBindTexture(GL_TEXTURE_2D, GL_NONE);
    return texture;
}

GLuint TextureCenter::alloc(uint8_t *rgba, int width, int height) {
    GLuint texture = alloc();
    glBindTexture(GL_TEXTURE_2D, texture);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE,
                 rgba);
    glBindTexture(GL_TEXTURE_2D, GL_NONE);
    return texture;
}

void TextureCenter::recycle(GLuint texture) {
    for (auto itr = textures.cbegin(); itr != textures.cend(); itr++) {
        if (GL_NONE != *itr && texture == *itr) {
            LOGI("TextureCenter %s %d", __func__, *itr);
            glDeleteTextures(1, &texture);
            textures.erase(itr);
        }
    }
}

void TextureCenter::clear() {
    for (auto itr = textures.cbegin(); itr != textures.cend(); itr++) {
        LOGI("TextureCenter %s %d", __func__, *itr);
        glDeleteTextures(1, reinterpret_cast<const GLuint *>(*itr));
        textures.erase(itr);
    }
}