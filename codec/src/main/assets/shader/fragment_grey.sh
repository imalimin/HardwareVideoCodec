//使用外部纹理必须支持此扩展
precision highp float;
//外部纹理采样器
uniform sampler2D uTexture;
varying vec2 vTextureCoord;
//const highp vec3 W = vec3(0.3, 0.59, 0.11);
const highp vec3 W = vec3(0.2125, 0.7154, 0.0721);
void main(){
    lowp vec4 textureColor = texture2D(uTexture, vTextureCoord);
    float luminance = dot(textureColor.rgb, W);
    gl_FragColor = vec4(vec3(luminance), textureColor.a);
}
