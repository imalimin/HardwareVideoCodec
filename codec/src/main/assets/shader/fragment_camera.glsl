#extension GL_OES_EGL_image_external : require
precision mediump float;
varying mediump vec2 vTextureCoord;
uniform samplerExternalOES uTexture;
void main(){
    vec4 color = vec4(texture2D(uTexture, vTextureCoord).rgb, 1.0);
    gl_FragColor = color;
}