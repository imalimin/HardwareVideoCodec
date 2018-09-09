precision mediump float;

uniform float time;
uniform sampler2D sTexture;
varying vec2 vTextureCoord;

void main() {
    vec2 uv = vTextureCoord.xy;

	float amount = 0.0;

	amount = (1.0 + sin(time*6.0)) * 0.5;
	amount *= 1.0 + sin(time*16.0) * 0.5;
	amount *= 1.0 + sin(time*19.0) * 0.5;
	amount *= 1.0 + sin(time*27.0) * 0.5;
	amount = pow(amount, 3.0);

	amount *= 0.05;

    vec3 col;
    col.r = texture2D( sTexture, vec2(uv.x+amount,uv.y) ).r;
    col.g = texture2D( sTexture, uv ).g;
    col.b = texture2D( sTexture, vec2(uv.x-amount,uv.y) ).b;

	col *= (1.0 - amount * 0.5);

    gl_FragColor = vec4(col,1.0);
}