precision mediump float;
varying vec2 vTextureCoord;
uniform sampler2D uTexture;  // 高反差保留的高斯模糊纹理
uniform sampler2D sTexture2;         // 输入原图
uniform sampler2D sTexture3;          // 原图的高斯模糊纹理
const float intensity = 1.0;           // 磨皮程度
void main() {
    lowp vec4 sourceColor = texture2D(sTexture2, vTextureCoord);
    lowp vec4 blurColor = texture2D(sTexture3, vTextureCoord);
    lowp vec4 highPassBlurColor = texture2D(uTexture, vTextureCoord);
    // 调节蓝色通道值
    mediump float value = clamp((min(sourceColor.b, blurColor.b) - 0.2) * 5.0, 0.0, 1.0);
    // 找到模糊之后RGB通道的最大值
    mediump float maxChannelColor = max(max(highPassBlurColor.r, highPassBlurColor.g), highPassBlurColor.b);
    // 计算当前的强度
    mediump float currentIntensity = (1.0 - maxChannelColor / (maxChannelColor + 0.2)) * value * intensity;
    // 混合输出结果
    lowp vec3 resultColor = mix(sourceColor.rgb, blurColor.rgb, currentIntensity);
    // 输出颜色
    gl_FragColor = vec4(resultColor, 1.0);
}