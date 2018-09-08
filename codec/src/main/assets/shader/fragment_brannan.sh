//lvjing4

precision lowp float;

varying highp vec2 vTextureCoord;

uniform sampler2D sTexture;
uniform sampler2D sTexture2;  //process
uniform sampler2D sTexture3;  //blowout
uniform sampler2D sTexture4;  //contrast
uniform sampler2D sTexture5;  //luma
uniform sampler2D sTexture6;  //screen

mat3 saturateMatrix = mat3(
                    1.105150,
                    -0.044850,
                    -0.046000,
                    -0.088050,
                    1.061950,
                    -0.089200,
                    -0.017100,
                    -0.017100,
                    1.132900);

vec3 luma = vec3(.3, .59, .11);

void main()
{

    vec3 texel = texture2D(sTexture, vTextureCoord).rgb;

    vec2 lookup;
    lookup.y = 0.5;
    lookup.x = texel.r;
    texel.r = texture2D(sTexture2, lookup).r;
    lookup.x = texel.g;
    texel.g = texture2D(sTexture2, lookup).g;
    lookup.x = texel.b;
    texel.b = texture2D(sTexture2, lookup).b;

    texel = saturateMatrix * texel;

    vec2 tc = (2.0 * vTextureCoord) - 1.0;
    float d = dot(tc, tc);
    vec3 sampled;
    lookup.y = 0.5;
    lookup.x = texel.r;
    sampled.r = texture2D(sTexture3, lookup).r;
    lookup.x = texel.g;
    sampled.g = texture2D(sTexture3, lookup).g;
    lookup.x = texel.b;
    sampled.b = texture2D(sTexture3, lookup).b;
    float value = smoothstep(0.0, 1.0, d);
    texel = mix(sampled, texel, value);

    lookup.x = texel.r;
    texel.r = texture2D(sTexture4, lookup).r;
    lookup.x = texel.g;
    texel.g = texture2D(sTexture4, lookup).g;
    lookup.x = texel.b;
    texel.b = texture2D(sTexture4, lookup).b;


    lookup.x = dot(texel, luma);
    texel = mix(texture2D(sTexture5, lookup).rgb, texel, .5);

    lookup.x = texel.r;
    texel.r = texture2D(sTexture6, lookup).r;
    lookup.x = texel.g;
    texel.g = texture2D(sTexture6, lookup).g;
    lookup.x = texel.b;
    texel.b = texture2D(sTexture6, lookup).b;

    gl_FragColor = vec4(texel, 1.0);

    gl_FragColor = vec4(((gl_FragColor.rgb - vec3(0.5)) * 0.7 + vec3(0.5)), gl_FragColor.w);

}