package worldofcaves;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public final class Shader
{
    private Shader() {}
    
    /*private static int blockVert, blockFrag, basicTransformVert, blackTransparentFrag;
    public static int blockProg, blockSelectionBoxProg;
    public static int blockMatrixUniform, blockSelectionBoxMatrixUniform;*/
    public static ShaderProgram block, blockSelectionBox, basicModels, sun, lightmap, post;
    
    private static boolean isDestroyed = false;
    
    public static void load()
    {
        WorldOfCaves.log("Loading shaders...");
        
        block = new ShaderProgram(
                "shaders/vertex/block.vert",
                "shaders/fragment/block.frag",
                new String[] {"in_Position", "in_TextureCoord", "in_NormalIndex"},
                new String[] {"in_Matrix", "texture_diffuse", "sunAngle", "in_LightMatrix",
                    "lightmap", "lightmapXSize", "lightmapYSize", "shadowMode", "fogColor"});
        
        blockSelectionBox = new ShaderProgram(
                "shaders/vertex/basicTransform.vert",
                "shaders/fragment/blackTransparent.frag",
                new String[] {"in_Position"},
                new String[] {"in_Matrix"});
        
        basicModels = new ShaderProgram(
                "shaders/vertex/basic.vert",
                "shaders/fragment/basic.frag",
                new String[] {"in_Position", "in_TextureCoord"},
                new String[] {"in_Matrix"});
        
        sun = new ShaderProgram(
                "shaders/vertex/sun.vert",
                "shaders/fragment/sun.frag",
                new String[] {"in_Position"},
                new String[] {"in_Matrix"});
        
        lightmap = new ShaderProgram(
                "shaders/vertex/lightmap.vert",
                "shaders/fragment/lightmap.frag",
                new String[] {"in_Position"},
                new String[] {"in_Matrix"});
        
        post = new ShaderProgram(
                "shaders/vertex/post.vert",
                "shaders/fragment/post.frag",
                new String[] {"in_Position", "in_TextureCoord"},
                new String[] {"screenSize", "seed"});
        
        WorldOfCaves.checkGLErrors("Shader.load()");
    }
    
    public static void unload()
    {
        WorldOfCaves.log("Unloading shaders...");
        
        try
        {
            block.destroy();
            blockSelectionBox.destroy();
            basicModels.destroy();
            sun.destroy();
            lightmap.destroy();
            post.destroy();
        }
        catch (NullPointerException e)
        {
            WorldOfCaves.logError("Unable to unload shaders!");
        }
        
        isDestroyed = true;
        
        WorldOfCaves.checkGLErrors("Shader.unload()");
    }
    
    @Override
    public void finalize()
    {
        if (!isDestroyed)
        {
            WorldOfCaves.logError("Shaders not properly unloaded.");
            unload();
        }
        
        try
        {
            super.finalize();
        }
        catch (Throwable e)
        {
            WorldOfCaves.logError("Error in Shader.finalize()", e);
        }
    }
    
    public static int loadShader(String filename, int type)
    {
        StringBuilder shaderSource = new StringBuilder();
        int shaderId = 0;
        BufferedReader reader = null;
        
        try
        {
            reader = new BufferedReader(new InputStreamReader(Util.getResourceAsStream(filename)));
            String line;
            
            while ((line = reader.readLine()) != null)
            {
                shaderSource.append(line).append("\n");
            }
            reader.close();
        }
        catch (IOException e)
        {
            WorldOfCaves.forceExit("Error reading shader file: " + filename, e);
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                    WorldOfCaves.forceExit("Error reading shader file: " + filename, e);
                }
            }
        }
        
        shaderId = glCreateShader(type);
        glShaderSource(shaderId, shaderSource);
        glCompileShader(shaderId);
        
        int status = glGetShaderi(shaderId, GL_COMPILE_STATUS);
        
        if (status != GL_TRUE)
        {
            String log = glGetShaderInfoLog(shaderId, 1024);
            WorldOfCaves.forceExit("Error compiling shader \"" + filename + "\":\n" + log);
        }
        
        WorldOfCaves.checkGLErrors("Shader.loadShader(" + filename + ")");
        
        return shaderId;
    }
}
