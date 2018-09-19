 precision lowp float;

 varying highp vec2 vTextureCoord;

 uniform sampler2D sTexture;
 uniform sampler2D sTexture2; //map
 uniform sampler2D sTexture3;

 void main()
 {

vec3 texel = texture2D(sTexture, vTextureCoord).rgb;


     texel = vec3(
                  texture2D(sTexture2, vec2(texel.r, .16666)).r,
                  texture2D(sTexture2, vec2(texel.g, .5)).g,
                  texture2D(sTexture2, vec2(texel.b, .83333)).b);

     vec2 tc = (2.0 * vTextureCoord) - 1.0;
     float d = dot(tc, tc);
     vec2 lookup = vec2(d, texel.r);
     texel.r = texture2D(sTexture3, lookup).r;
     lookup.y = texel.g;
     texel.g = texture2D(sTexture3, lookup).g;
     lookup.y = texel.b;
     texel.b= texture2D(sTexture3, lookup).b;

     gl_FragColor = vec4(texel, 1.0);
 }