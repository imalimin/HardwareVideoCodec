precision lowp float;

varying highp vec2 vTextureCoord;

uniform sampler2D sTexture;
uniform sampler2D sTexture2;
uniform sampler2D sTexture3;

mat3 saturateMatrix = mat3(
1.1402,
-0.0598,
-0.061,
-0.1174,
1.0826,
-0.1186,
-0.0228,
-0.0228,
1.1772);

vec3 lumaCoeffs = vec3(.3, .59, .11);

void main()
{

vec3 texel = texture2D(sTexture, vTextureCoord).rgb;


texel = vec3(
texture2D(sTexture2, vec2(texel.r, .1666666)).r,
texture2D(sTexture2, vec2(texel.g, .5)).g,
texture2D(sTexture2, vec2(texel.b, .8333333)).b
);

texel = saturateMatrix * texel;
float luma = dot(lumaCoeffs, texel);
texel = vec3(
texture2D(sTexture3, vec2(luma, texel.r)).r,
texture2D(sTexture3, vec2(luma, texel.g)).g,
texture2D(sTexture3, vec2(luma, texel.b)).b);


gl_FragColor = vec4(texel, 1.0);

}