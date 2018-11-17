#version 150

uniform vec2 screenSize;

in vec4 in_Position;
in vec2 in_TextureCoord;

out vec2 pass_TextureCoord;
out vec2 pass_ScreenCoord;

void main()
{
    gl_Position = in_Position;
    pass_TextureCoord = in_TextureCoord;
    
    pass_ScreenCoord = (in_Position.xy + 1) / 2.0 * screenSize;
}