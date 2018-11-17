#version 150 core

uniform sampler2DArray texture_diffuse;
uniform sampler2D lightmap;
uniform float lightmapXSize;
uniform float lightmapYSize;
uniform int shadowMode;
uniform float sunAngle;
uniform vec3 fogColor;
//uniform float fogStart;
//uniform float fogEnd;

in vec3 pass_LightmapCoord;
in vec3 pass_TextureCoord;
in vec3 pass_Normal;
in vec3 pass_Ambient;
in vec3 pass_Diffuse;

out vec4 out_Color;

//const float middleSample = 9.0/9;//4 / (4 + 3 * sqrt(2.0));
//const float sideSample = 9.0/9;//4 * 0.75 / (4 + 3 * sqrt(2.0));
//const float cornerSample = 9.0/9;//4 * sqrt(2.0) / (4 + 3 * sqrt(2.0));
float shadowBias = 0.25/lightmapXSize;

float getShadow(in vec3 pos)
{
    float delta = texture(lightmap, pos.xy).x - pos.z + shadowBias;
    
    if (delta < 0)
    {
        return clamp(-delta * 1536, 0, 1);
    }
    
    return 0;
}

void main()
{
    out_Color = texture(texture_diffuse, pass_TextureCoord);
    
    if (out_Color.a == 0)
    {
        discard;
    }
    
    vec3 diffuse = pass_Diffuse;
    
    if (shadowMode == 2)
    {
        float shadow = 0;
        
        if    (pass_LightmapCoord.x > 0 && pass_LightmapCoord.x < 1
            && pass_LightmapCoord.y > 0 && pass_LightmapCoord.y < 1)
        {
            float xStep = 1/lightmapXSize;
            float yStep = 1/lightmapYSize;
            
            vec2 lightmapSize = vec2(lightmapXSize, lightmapYSize);
            vec2 texelSize = 1 / lightmapSize;
            vec2 sclPos = pass_LightmapCoord.xy * lightmapSize + vec2(0.5, 0.5);
            vec2 pos1 = (floor(sclPos) - vec2(0.5, 0.5)) * texelSize;
            vec2 fracPos = fract(sclPos);
            vec2 invFracPos = 1 - fracPos;
            
            float s1 = getShadow(vec3(pos1.x, pos1.y, pass_LightmapCoord.z));
            float s2 = getShadow(vec3(pos1.x + texelSize.x, pos1.y, pass_LightmapCoord.z));
            
            float s3 = getShadow(vec3(pos1.x, pos1.y + texelSize.y, pass_LightmapCoord.z));
            float s4 = getShadow(vec3(pos1.x + texelSize.x, pos1.y + texelSize.y, pass_LightmapCoord.z));
            
            float m1 = mix(s1, s3, fracPos.y);
            float m2 = mix(s2, s4, fracPos.y);
            
            float sh = sin(sunAngle);
            float ss = cos(sunAngle);
            
            shadow = mix(m1, m2, fracPos.x);
            
            if (sh < 0.1)
            {
                if (ss < 0)
                {
                    shadow = mix(m2, shadow, clamp(sh * 10, 0, 1));
                }
                else
                {
                    shadow = mix(m1, shadow, clamp(sh * 10, 0, 1));
                }
            }
            else if (abs(ss) < 0.1)
            {
                if (pass_Normal.x >  0.99 && ss > 0
                 || pass_Normal.x < -0.99 && ss < 0)
                {
                    if (ss < 0)
                    {
                        shadow = mix(m1, shadow, clamp(-ss * 10, 0, 1));
                    }
                    else
                    {
                        shadow = mix(m2, shadow, clamp(ss * 10, 0, 1));
                    }
                }
            }
            
            /*else
            {
                shadow = m1;
            }*/
        }
        
        shadow = clamp(3 - shadow * 3, 0, 1);
        //if (shadow > 0)
        {
            //shadow = 1;
        }
        diffuse *= shadow;
    }
    
    const float fogStart = 384;
    const float fogEnd = 512;
    float dist = gl_FragCoord.z / gl_FragCoord.w;
    float fog;
    
    if (dist <= fogStart)
    {
        fog = 0;
    }
    else if (dist >= fogEnd)
    {
        fog = 1;
    }
    else
    {
        fog = 1 - clamp((fogEnd - dist) / (fogEnd - fogStart), 0, 1);
    }
    
    out_Color.rgb *= pass_Ambient + diffuse;
    out_Color.rgb = mix(out_Color.rgb, fogColor, fog);
}