precision mediump float;
varying mediump vec2 vTextureCoord;
uniform sampler2D uTexture;
uniform float width; // is original image width, it means twice as wide as ours

void main()
{
        vec4 rgba1, rgba2;
        vec4 yuv1, yuv2;
        vec2 coor1, coor2;
        float U, V;

        coor1 = vTextureCoord.xy - vec2(1.0 / (width * 2.0), 0.0);
        coor2 = vTextureCoord.xy + vec2(1.0 / (width * 2.0), 0.0);

        rgba1  = texture2D(uTexture, coor1);
        rgba2  = texture2D(uTexture, coor2);
        
        yuv1.x = 1.0/16.0 + (rgba1.r * 0.2126 + rgba1.g * 0.7152 + rgba1.b * 0.0722) * 0.8588; // Y
        yuv1.y = 0.5 + (-rgba1.r * 0.1145 - rgba1.g * 0.3854 + rgba1.b * 0.5) * 0.8784;
        yuv1.z = 0.5 + (rgba1.r * 0.5 - rgba1.g * 0.4541 - rgba1.b * 0.0458) * 0.8784;
        
        yuv2.x = 1.0/16.0 + (rgba2.r * 0.2126 + rgba2.g * 0.7152 + rgba2.b * 0.0722) * 0.8588; // Y
        yuv2.y = 0.5 + (-rgba2.r * 0.1145 - rgba2.g * 0.3854 + rgba2.b * 0.5) * 0.8784;
        yuv2.z = 0.5 + (rgba2.r * 0.5 - rgba2.g * 0.4541 - rgba2.b * 0.0458) * 0.8784;
        
        U = mix(yuv1.y, yuv2.y, 0.5);
        V = mix(yuv1.z, yuv2.z, 0.5);
        
        gl_FragColor = vec4(yuv1.x, U, yuv2.x, V);
}