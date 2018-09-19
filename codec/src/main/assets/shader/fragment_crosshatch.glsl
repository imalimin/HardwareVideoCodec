varying highp vec2 vTextureCoord;

uniform sampler2D uTexture;
uniform highp float crossHatchSpacing;
uniform highp float lineWidth;
const highp vec3 W = vec3(0.2125, 0.7154, 0.0721);

void main(){
    highp float luminance = dot(texture2D(uTexture, vTextureCoord).rgb, W);
    lowp vec4 colorToDisplay = vec4(1.0, 1.0, 1.0, 1.0);
    if (luminance < 1.00){
        if (mod(vTextureCoord.x + vTextureCoord.y, crossHatchSpacing) <= lineWidth){
            colorToDisplay = vec4(0.0, 0.0, 0.0, 1.0);
        }
    }
    if (luminance < 0.75){
        if (mod(vTextureCoord.x - vTextureCoord.y, crossHatchSpacing) <= lineWidth){
            colorToDisplay = vec4(0.0, 0.0, 0.0, 1.0);
        }
    }
    if (luminance < 0.50){
        if (mod(vTextureCoord.x + vTextureCoord.y - (crossHatchSpacing / 2.0), crossHatchSpacing) <= lineWidth){
            colorToDisplay = vec4(0.0, 0.0, 0.0, 1.0);
        }
    }
    if (luminance < 0.3){
        if (mod(vTextureCoord.x - vTextureCoord.y - (crossHatchSpacing / 2.0), crossHatchSpacing) <= lineWidth){
            colorToDisplay = vec4(0.0, 0.0, 0.0, 1.0);
        }
    }
    gl_FragColor = colorToDisplay;
}