#version 150 core

uniform mat4 in_Matrix;
uniform float sunAngle;
uniform mat4 in_LightMatrix;
uniform sampler2DShadow lightmap;
uniform float lightmapXSize;
uniform float lightmapYSize;
uniform int shadowMode;

in vec4 in_Position;
in vec3 in_TextureCoord;
in float in_NormalIndex;

out vec3 pass_LightmapCoord;
out vec3 pass_TextureCoord;
out vec3 pass_Normal;
out vec3 pass_Ambient;
out vec3 pass_Diffuse;

void main()
{
    gl_Position = in_Matrix * in_Position;
    
    pass_LightmapCoord = (in_LightMatrix * in_Position).xyz;
    
    pass_TextureCoord.xy = in_TextureCoord.xy / 16;
    pass_TextureCoord.z = in_TextureCoord.z;
    
    vec3 normal;
    
    switch (int(in_NormalIndex))
    {
        case 0:
            normal = vec3(-1, 0, 0);
            break;
        case 1:
            normal = vec3(1, 0, 0);
            break;
        case 2:
            normal = vec3(0, -1, 0);
            break;
        case 3:
            normal = vec3(0, 1, 0);
            break;
        case 4:
            normal = vec3(0, 0, -1);
            break;
        case 5:
            normal = vec3(0, 0, 1);
            break;
    }
    pass_Normal = normal;
    //ignore if night
    float d = sin(sunAngle);
    vec3 l = vec3(cos(sunAngle), 0, d);
    float cosTheta = clamp(dot(normal, l), 0, 1);
    float sl = sqrt(clamp(d, 0, 1));
    
    float i;
    if (in_NormalIndex == 4)
    {
        i = (cosTheta * 0.375 + 0.25) * sl;
    }
    else
    {
        const float ex = 1;
        float a;
        if (in_NormalIndex >= 2)//(in_NormalIndex == 2 || in_NormalIndex == 3)
        {
            a = (pow(cosTheta, ex) + pow(clamp(max(max(dot(vec3(-1, 0, 0), l), dot(vec3(1, 0, 0), l)), cosTheta), 0, 1), ex)) / 2;
        }
        else
        {
            a = pow(cosTheta, ex);
        }
        i = (mix(cosTheta, 1, pow(sl, 4)) * 0.375 + 0.25 * a) * sl;
        //pass_Diffuse = vec3(a, a, a);
    }
    
    float sr = pow(1 - min(abs(d-.025), 1), 5);
    
    float pass_ShadowBias = (cosTheta - 0.9) / 0.1;//clamp(0.005*tan(acos(cosTheta)), 0, 0.01); // cosTheta is dot( n,l ), clamped between 0 and 1
    
    //vec3(.992, .370, .326)
    pass_Diffuse = vec3(i, i, i);
    pass_Diffuse *= mix(vec3(1, 1, 1), vec3(.992, .195, .163), clamp(sr, 0, 1)) * 2;
    
    pass_Ambient = vec3(0.0625, 0.0625, 0.0625) * 2;
}