attribute vec2 aPosition;
attribute vec2 aTextureCoord;
varying vec2 vTextureCoord;
uniform mat2 rotation;
uniform vec2 flipScale;
void main()
{
    gl_Position = vec4(aPosition, 0.0, 1.0);
    gl_Position.y = (gl_Position.y + 1.0) * 8.0 / 3.0 - 1.0;
    vTextureCoord = flipScale * (aPosition / 2.0 * rotation) + 0.5;
}