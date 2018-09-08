//lvjing5

precision lowp float;

varying highp vec2 vTextureCoord;

uniform sampler2D sTexture;
uniform sampler2D sTexture2;

void main()
{

vec3 texel = texture2D(sTexture, vTextureCoord).rgb;

//=======

texel = vec3(dot(vec3(0.3, 0.6, 0.1), texel));
texel = vec3(texture2D(sTexture2, vec2(texel.r, .16666)).r);
gl_FragColor = vec4(texel, 1.0);
}