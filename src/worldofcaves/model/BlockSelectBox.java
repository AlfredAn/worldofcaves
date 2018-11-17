package worldofcaves.model;

import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import worldofcaves.MatrixHandler;
import worldofcaves.Shader;
import worldofcaves.WorldOfCaves;

public class BlockSelectBox extends Model
{
    private static final byte[] cube =
        {
            0, 0, 0, //0
            0, 0, 1, //1
            0, 1, 0, //2
            0, 1, 1, //3
            
            1, 0, 0, //4
            1, 0, 1, //5
            1, 1, 0, //6
            1, 1, 1  //7
        };
    
    private static final byte[] cubeIndex =
        {
            0, 1,
            0, 2,
            0, 4,
            
            1, 3,
            1, 5,
            
            2, 3,
            2, 6,
            
            3, 7,
            
            4, 5,
            4, 6,
            
            5, 7,
            
            6, 7,
        };
    
    @Override
    public void render()
    {
        if (isFinished)
        {
            throw new IllegalStateException("BlockSelectBox model is already rendered.");
        }
        
        int elementCount = 3;
        int byteSize = elementCount * 1;
        
        ByteBuffer vertexBuffer = BufferUtils.createByteBuffer(cube.length);
        ByteBuffer indexBuffer = BufferUtils.createByteBuffer(cubeIndex.length);
        
        indexCount = cubeIndex.length;
        
        vertexBuffer.put(cube);
        vertexBuffer.flip();
        
        indexBuffer.put(cubeIndex);
        indexBuffer.flip();
        
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);
        
        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 3, GL_UNSIGNED_BYTE, false, byteSize, 0);
        
        vboiId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboiId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
        
        isFinished = true;
        
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        
        glBindVertexArray(0);
        
        WorldOfCaves.checkGLErrors("BlockSelectBox.render()");
    }
    
    public void draw(MatrixHandler mh)
    {
        if (!isFinished)
        {
            throw new IllegalStateException("This model is not yet finished.");
        }
        
        Shader.blockSelectionBox.use();
        
        mh.uniformMVP(Shader.blockSelectionBox.getUniformLocation(0));
        
        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboiId);
        
        glDrawElements(GL_LINES, indexCount, GL_UNSIGNED_BYTE, 0);
        
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
        glUseProgram(0);
        
        WorldOfCaves.checkGLErrors("BlockSelectBox.draw()");
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
        
        WorldOfCaves.checkGLErrors("BlockSelectBox.destroy()");
    }
}