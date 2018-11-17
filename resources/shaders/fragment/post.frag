#version 150

uniform sampler2D sampler;
uniform int seed;

in vec2 pass_TextureCoord;
in vec2 pass_ScreenCoord;

out vec4 out_Color;

const float maxD = sqrt(3.0);

float noise(in int n)
{
    n = n ^ seed;
    n = (n >> 13) ^ n;
    int nn = (n * (n * n * 60493 + 19990303) + 1376312589) & 0x7fffffff;
    return 1.0 - (float(nn) / 1073741824.0);
}

float noise(in int x, in int y)
{
    return noise(x + y * 7919) * noise(-x * 7919 - y);
}

void main()
{
    out_Color = texture(sampler, pass_TextureCoord);
    
    float noise = noise(int(pass_ScreenCoord.x), int(pass_ScreenCoord.y));
    float brightness = distance(vec3(0, 0, 0), out_Color.rgb) / maxD;
    float noiseStrength = pow(1 - brightness, 2) / 16 * clamp(brightness * 64, 0, 1);
    
    if (brightness == 0)
    {
        out_Color.rgb = vec3(noiseStrength * noise, noiseStrength * noise, noiseStrength * noise);
    }
    else
    {
        float newBrightness = max(brightness + noiseStrength * noise, 0);
        float brightnessRatio = newBrightness / brightness;
        out_Color.rgb *= brightnessRatio;
    }
    
    //float d = distance(vec3(0, 0, 0), out_Color.rgb) / maxD;
    //out_Color.rgb = vec3(d, d, d);
}