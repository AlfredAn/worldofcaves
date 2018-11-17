#version 150

uniform mat4 in_Matrix;

in vec4 in_Position;

void main()
{
    gl_Position = in_Matrix * in_Position;
}