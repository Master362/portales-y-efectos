#version 150

uniform sampler2D Scene;
uniform float Time;
uniform float Intensity;
uniform float Aspect;
uniform float Phase; // 0 = absorcion (entrada), 1 = llegada

in vec2 texCoord;
out vec4 fragColor;

void main() {
    float I = clamp(Intensity, 0.0, 1.0);
    vec2 uv = texCoord;
    vec2 p = (uv - 0.5) * vec2(Aspect, 1.0);
    float r = length(p);
    float ang = atan(p.y, p.x);

    // torbellino: gira mas fuerte en el centro
    float swirl = I * 2.6 * (1.0 - smoothstep(0.0, 0.9, r));
    float s = sin(swirl), c = cos(swirl);
    vec2 q = vec2(c * p.x - s * p.y, s * p.x + c * p.y);

    // absorcion: el espacio se pliega hacia el centro (o se expande al llegar)
    float pull = mix(0.55, 0.35, Phase);
    q *= 1.0 - pull * I * (1.0 - smoothstep(0.0, 1.1, r));

    // ondas dimensionales concentricas
    float wave = sin(r * 38.0 - Time * 9.0) * 0.012 * I;
    q += normalize(p + 1e-5) * wave;

    vec2 suv = q / vec2(Aspect, 1.0) + 0.5;

    // aberracion cromatica
    float ab = 0.012 * I * (0.4 + r);
    vec2 dir = normalize(p + 1e-5) / vec2(Aspect, 1.0);
    vec3 col;
    col.r = texture(Scene, clamp(suv + dir * ab, 0.001, 0.999)).r;
    col.g = texture(Scene, clamp(suv, 0.001, 0.999)).g;
    col.b = texture(Scene, clamp(suv - dir * ab, 0.001, 0.999)).b;

    // tinte verde energetico
    float lum = dot(col, vec3(0.299, 0.587, 0.114));
    vec3 green = vec3(0.10, 1.0, 0.35) * (0.35 + lum);
    col = mix(col, green, smoothstep(0.0, 0.9, I) * 0.85);

    // rayos de energia radiales
    float rays = pow(max(0.0, sin(ang * 14.0 + Time * 3.0 + r * 8.0)), 6.0);
    float core = exp(-r * 3.2);
    vec3 energy = vec3(0.25, 1.0, 0.5) * (rays * 0.55 + core * 0.9);
    col += energy * I * (0.4 + 0.6 * sin(Time * 12.0) * 0.5 + 0.3);

    // vineta verde y destello blanco-verde en el pico
    float vig = smoothstep(0.35, 1.0, r);
    col = mix(col, vec3(0.0, 0.55, 0.15), vig * I * 0.75);
    float flash = smoothstep(0.85, 1.0, I) * core;
    col = mix(col, vec3(0.85, 1.0, 0.9), flash * 0.7);

    fragColor = vec4(col, 1.0);
}
