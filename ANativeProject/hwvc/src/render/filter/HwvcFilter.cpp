/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/HwvcFilter.h"
#include "../include/NormalDrawer.h"
#include "../entity/FilterEntity.h"
#include "log.h"

HwvcFilter::HwvcFilter(char *path) {
    reader = new FilterReader(path);
    decoder = new PngDecoder();
}

HwvcFilter::~HwvcFilter() {
    if (params) {
        delete[]params;
        params = nullptr;
    }
    if (paramLocations) {
        delete[]paramLocations;
        paramLocations = nullptr;
    }
    if (textures) {
        glDeleteTextures(size, textures);
        delete[]textures;
        textures = nullptr;
    }
    if (textureLocations) {
        delete[]textureLocations;
        textureLocations = nullptr;
    }
    if (decoder) {
        delete decoder;
        decoder = nullptr;
    }
    if (reader) {
        delete reader;
        reader = nullptr;
    }
}

bool HwvcFilter::init(int w, int h) {
    if (!Filter::init(w, h))
        return false;
    struct timeval start, end;
    gettimeofday(&start, NULL);
    FilterEntity *entity = reader->read();
    gettimeofday(&end, NULL);
    long time = end.tv_usec - start.tv_usec;
    drawer = new NormalDrawer(entity->vertex, entity->fragment);
    //读取Sampler
    this->size = entity->samplers.size();
    if (0 != this->size) {
        textures = new GLuint[size];
        textureLocations = new GLint[size];
        int i = 0;
        for (auto itr = entity->samplers.begin(); itr != entity->samplers.end(); itr++) {
            textureLocations[i] = drawer->getUniformLocation(itr->first);
            textures[i] = loadTexture(itr->second);
            ++i;
        }
    }
    //读取Params
    paramSize = entity->params.size();
    if (0 != paramSize) {
        paramLocations = new GLint[paramSize];
        params = new float[paramSize];
        int i = 0;
        for (auto itr = entity->params.begin(); itr != entity->params.end(); itr++) {
            paramLocations[i] = drawer->getUniformLocation(itr->first);
            params[i] = itr->second;
            ++i;
        }
    }
    gettimeofday(&end, NULL);
    LOGI("%s(ver: %d) read cost: %ld / %ld us", entity->name.c_str(), entity->version, time,
         (end.tv_usec - start.tv_usec));
    delete entity;
    return true;
}

void HwvcFilter::bindResources() {
    Filter::bindResources();
    /**
     * GL_TEXTURE0为保留Sampler，给默认画面使用
     */
    for (int i = 0; i < size; ++i) {
        int offset = i + 1;
        glActiveTexture(static_cast<GLenum>(GL_TEXTURE0 + offset));
        glBindTexture(GL_TEXTURE_2D, textures[i]);
        glUniform1i(textureLocations[i], offset);
    }
    for (int i = 0; i < paramSize; ++i) {
        drawer->setUniform1f(paramLocations[i], params[i]);
    }
}

GLuint HwvcFilter::loadTexture(string pngBuf) {
    if (!decoder) {
        LOGE("Decoder is null");
        return GL_NONE;
    }
    uint8_t *rgba;
    int width = 0, height = 0;
    int ret = decoder->decodeBuf((uint8_t *) pngBuf.data(), pngBuf.size(), &rgba, &width, &height);
    if (!ret) {
        LOGE("Read image failed(%d)", ret);
        return GL_NONE;
    }
    GLuint texture = GL_NONE;
    glGenTextures(1, &texture);
    if (GL_NONE == texture)
        return GL_NONE;
    glBindTexture(GL_TEXTURE_2D, texture);
    glTexParameterf(GL_TEXTURE_2D,
                    GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D,
                    GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D,
                    GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameterf(GL_TEXTURE_2D,
                    GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, rgba);
    glBindTexture(GL_TEXTURE_2D, GL_NONE);
    delete[]rgba;
    return texture;
}