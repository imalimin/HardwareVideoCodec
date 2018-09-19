varying highp vec2 vTextureCoord;

uniform sampler2D uTexture;

uniform lowp float distance;
uniform highp float slope;

void main(){

	 highp vec4 color = vec4(1.0);

	 highp float  d = vTextureCoord.y * slope  +  distance;

	 highp vec4 c = texture2D(uTexture, vTextureCoord) ; // consider using unpremultiply

	 c = (c - d * color) / (1.0 -d);

	 gl_FragColor = c; //consider using premultiply(c);
}