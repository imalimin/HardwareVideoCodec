precision mediump float;
varying mediump vec2 vTextureCoord;
uniform sampler2D uTexture;
void main(){
    vec4 color = texture2D(uTexture, vTextureCoord);
    gl_FragColor = color;
}