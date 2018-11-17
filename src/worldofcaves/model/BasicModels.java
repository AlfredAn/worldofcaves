package worldofcaves.model;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import worldofcaves.MatrixHandler;
import worldofcaves.Shader;
import worldofcaves.WorldOfCaves;

public class BasicModels extends Model
{
    public static final int QUAD = 0;
    
    private static final float[] xyz = new float[]
    {
        //quad
        -1, -1,  0,
        -1,  1,  0,
         1,  1,  0,
         1, -1,  0
    };
    
    private static final float[] st = new float[]
    {
        //quad
        0, 0,
        0, 1,
        1, 1,
        1, 0
    };
    
    private static final byte[] id = new byte[]
    {
        //quad
        2, 1, 0,
        0, 3, 2
    };
    
    @Override
    public void render()
    {
        if (isFinished)
        {
            throw new IllegalStateException("BasicModels model is already rendered.");
        }
        
        int elementCount = 5;
        int byteSize = elementCount * 4;
        
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(elementCount * xyz.length / 3);
        ByteBuffer indexBuffer = BufferUtils.createByteBuffer(id.length);
        
        for (int i = 0; i < xyz.length / 3; i++)
        {
            vertexBuffer.put(xyz[i*3]);
            vertexBuffer.put(xyz[i*3+1]);
            vertexBuffer.put(xyz[i*3+2]);
            
            vertexBuffer.put(st[i*2]);
            vertexBuffer.put(st[i*2+1]);
        }
        vertexBuffer.flip();
        
        indexBuffer.put(id);
        indexBuffer.flip();
        
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);
        
        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 3, GL_FLOAT, false, byteSize, 0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, byteSize, 3*4);
        
        vboiId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboiId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
        
        isFinished = true;
        
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        
        glBindVertexArray(0);
        
        WorldOfCaves.checkGLErrors("BasicModels.render()");
    }
    
    public void draw(MatrixHandler mh, int model)
    {
        if (!isFinished)
        {
            throw new IllegalStateException("This model is not yet finished.");
        }
        
        if (mh != null)
        {
            mh.uniformMVP(Shader.basicModels.getUniformLocation(0));
        }
        
        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboiId);
        
        int off, length;
        switch (model)
        {
            case QUAD:
                off = 0;
                length = 6;
                break;
            default:
                throw new IllegalArgumentException("Invalid model id: " + model);
        }
        
        glDrawElements(GL_TRIANGLES, length, GL_UNSIGNED_BYTE, off);
        
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);
        //glUseProgram(0);
        
        WorldOfCaves.checkGLErrors("BasicModels.draw()");
    }
    
    @Override
    public void destroy()
    {
        glDisableVertexAttribArray(0);
        
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(vboId);
        
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glDeleteBuffers(vboiId);
        
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
        
        isFinished = false;
        
        WorldOfCaves.checkGLErrors("BasicModels.destroy()");
    }
}