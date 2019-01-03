/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/YUV420PFilter.h"
#include "../include/NormalDrawer.h"

static const string VERTEX = SHADER(
        attribute vec4 aPosition;
        attribute vec2 aTextureCoord;
        varying vec2 vTextureCoord;
        void main(void)
        {
            gl_Position = aPosition;
            vTextureCoord = aTextureCoord;
        }
);

static const string FRAGMENT = SHADER(
        varying vec2 vTextureCoord;
        uniform sampler2D uTexture;//Y
        uniform sampler2D uTexture1;//U
        uniform sampler2D uTexture2;//V
        void main(void)
        {
            vec3 yuv;
            vec3 rgb;
            yuv.x = texture2D(uTexture, vTextureCoord).r;
            yuv.y = texture2D(uTexture1, vTextureCoord).r - 0.5;
            yuv.z = texture2D(uTexture2, vTextureCoord).r - 0.5;
            rgb = mat3( 1,       1,         1,
                        0,       -0.39465,  2.03211,
                        1.13983, -0.58060,  0) * yuv;
            gl_FragColor = vec4(rgb, 1);
        }
);

YUV420PFilter::YUV420PFilter() {
    name = __func__;
}

YUV420PFilter::~YUV420PFilter() {

}

bool YUV420PFilter::init(int w, int h) {
    if (!Filter::init(w, h))
        return false;
    drawer = new NormalDrawer(VERTEX, FRAGMENT);
    this->uLocation = drawer->getUniformLocation("uTexture1");
    this->vLocation = drawer->getUniformLocation("uTexture2");
    return true;
}

void YUV420PFilter::bindResources() {
    Filter::bindResources();
    glActiveTexture(static_cast<GLenum>(GL_TEXTURE0 + 1));
    glBindTexture(GL_TEXTURE_2D, this->uTexture);
    glUniform1i(this->uLocation, 1);

    glActiveTexture(static_cast<GLenum>(GL_TEXTURE0 + 2));
    glBindTexture(GL_TEXTURE_2D, this->vTexture);
    glUniform1i(this->vLocation, 2);
}

void YUV420PFilter::draw(GLuint yTexture, GLuint uTexture, GLuint vTexture) {
    this->uTexture = uTexture;
    this->vTexture = vTexture;
    Filter::draw(yTexture);
}