attribute vec4 aPosition;
attribute vec2 aTextureCoord;
varying vec2 vTextureCoord;
void main()
{
    gl_Position = vec4(aPosition.x, aPosition.y, 0.0, 1.0);
    gl_Position.y = (gl_Position.y + 1.0) * 8.0 / 3.0 - 1.0;
    vTextureCoord = aTextureCoord;
}