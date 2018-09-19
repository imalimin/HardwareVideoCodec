precision mediump float;
varying mediump vec2 vTextureCoord;
uniform sampler2D uTexture;
void main(){
    const float gamma = 1.0;
    const float exposure = 1.5;
    vec3 hdrColor = texture2D(uTexture, vTextureCoord).rgb;
    vec3 mapped = vec3(1.0) - exp(-hdrColor * exposure);
    //mapped = pow(mapped, vec3(1.0 / gamma));
    gl_FragColor = vec4(mapped, 1.0);
}