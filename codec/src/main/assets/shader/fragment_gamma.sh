varying highp vec2 vTextureCoord;
uniform sampler2D uTexture;
uniform lowp float gamma;
void main(){
    lowp vec4 textureColor = texture2D(uTexture, vTextureCoord);
    gl_FragColor = vec4(pow(textureColor.rgb, vec3(gamma)), textureColor.w);
}