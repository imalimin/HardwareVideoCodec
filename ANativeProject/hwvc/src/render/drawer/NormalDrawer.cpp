/*
 * Copyright (c) 2018-present, lmyooyo@gmail.com.
 *
 * This source code is licensed under the GPL license found in the
 * LICENSE file in the root directory of this source tree.
 */
#include "../include/NormalDrawer.h"
#include "log.h"

static const string VERTEX = SHADER(
        attribute vec4 aPosition;
        attribute vec2 aTextureCoord;
        varying vec2 vTextureCoord;
        void main(){
            gl_Position= aPosition;
            vTextureCoord = aTextureCoord;
        }
);
static const string FRAGMENT = SHADER(
        precision mediump float;
        varying mediump vec2 vTextureCoord;
        uniform sampler2D uTexture;
        void main(){
            vec4 color = vec4(texture2D(uTexture, vTextureCoord).rgb, 1.0);
            gl_FragColor = color;
        }
);

NormalDrawer::NormalDrawer() : NormalDrawer(VERTEX, FRAGMENT) {
}

NormalDrawer::NormalDrawer(string vertex, string fragment) {
    this->vertex = vertex;
    this->fragment = fragment;
    init();
}

NormalDrawer::~NormalDrawer() {
}

GLuint NormalDrawer::getProgram() {
    return createProgram(this->vertex, this->fragment);
}
