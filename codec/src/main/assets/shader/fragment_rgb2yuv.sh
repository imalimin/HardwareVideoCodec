precision mediump float;
varying mediump vec2 vTextureCoord;
uniform sampler2D uTexture;
void main(){
    vec4 color = texture2D(uTexture, vTextureCoord);
    vec3 rgb = vec3(color.r, color.g, color.b);
    yuv = mat3(0.299,   0.587,   0.114,
               -0.1678, -0.3313, 0.5,
               0.5,     -0.4187, -0.0813) * rgb;
    gl_FragColor = vec4(yuv, 1);
}