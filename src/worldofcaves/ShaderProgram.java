package worldofcaves;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram
{
    private final int vert, frag, prog;
    private final int[] uniform;
    
    public ShaderProgram(String vertPath, String fragPath, String[] attribs, String[] uniforms)
    {
        if (attribs == null)
        {
            attribs = new String[0];
        }
        
        if (uniforms == null)
        {
            uniforms = new String[0];
        }
        
        vert = Shader.loadShader(vertPath, GL_VERTEX_SHADER);
        frag = Shader.loadShader(fragPath, GL_FRAGMENT_SHADER);
        
        prog = glCreateProgram();
        glAttachShader(prog, vert);
        glAttachShader(prog, frag);
        
        for (int i = 0; i < attribs.length; i++)
        {
            glBindAttribLocation(prog, i, attribs[i]);
        }
        
        glLinkProgram(prog);
        glValidateProgram(prog);
        
        int status = glGetProgrami(prog, GL_LINK_STATUS);
        
        if (status != GL_TRUE)
        {
            String log = glGetProgramInfoLog(prog, 1024);
            WorldOfCaves.forceExit("Error linking shader \"" + vertPath + "\" or \"" + fragPath + "\":\n" + log);
        }
        
        uniform = new int[uniforms.length];
        for (int i = 0; i < uniforms.length; i++)
        {
            uniform[i] = glGetUniformLocation(prog, uniforms[i]);
        }
        
        WorldOfCaves.checkGLErrors("ShaderProgram()");
    }
    
    public void destroy()
    {
        glUseProgram(0);
        glDetachShader(prog, vert);
        glDetachShader(prog, frag);
        
        glDeleteShader(vert);
        glDeleteShader(frag);
        glDeleteProgram(prog);
        
        WorldOfCaves.checkGLErrors("ShaderProgram.destroy()");
    }
    
    public void use()
    {
        glUseProgram(prog);
    }
    
    public int getUniformLocation(int i)
    {
        return uniform[i];
    }
}





