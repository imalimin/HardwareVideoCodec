precision mediump float;
varying mediump vec2 vTextureCoord;
uniform sampler2D uTexture;
void main(){
    vec4 color = vec4(texture2D(uTexture, vTextureCoord).rgb, 1.0);
    gl_FragColor = color;
}