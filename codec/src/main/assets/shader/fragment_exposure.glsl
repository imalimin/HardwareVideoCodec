varying highp vec2 vTextureCoord;

uniform sampler2D uTexture;
uniform highp float exposure;

void main(){
    highp vec4 textureColor = texture2D(uTexture, vTextureCoord);

    gl_FragColor = vec4(textureColor.rgb * pow(2.0, exposure), textureColor.w);
}