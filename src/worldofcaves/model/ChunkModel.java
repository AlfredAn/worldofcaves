package worldofcaves.model;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import worldofcaves.game.Chunk;
import worldofcaves.MatrixHandler;
import worldofcaves.WorldOfCaves;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import worldofcaves.game.block.Block;

public class ChunkModel extends Model
{
    private int num;
    private boolean isEmpty = false, isDestroyed = false;
    public final int xPos, yPos, zPos;
    
    private static final int elementCount = 7,
            byteSize = elementCount * 1;
    
    public static final int vboSize = Chunk.modelXSize * Chunk.modelYSize * Chunk.modelZSize
                    * 24 * byteSize;
    public static final int vboiSize = Chunk.modelXSize * Chunk.modelYSize * Chunk.modelZSize
                    * 6 * 6 * 4;
    
    private float basePriority = 1;
    private boolean absPriority = false;
    
    public final Chunk chunk;
    
    public final Object destroyLock = new Object();
    
    private static final byte[] cube =
        {
            //left (x=0)
            0, 0, 0,
            0, 0, 1,
            0, 1, 1,
            0, 1, 0,
            //right (x=1)
            1, 0, 0,
            1, 0, 1,
            1, 1, 1,
            1, 1, 0,
            //front (y=0)
            0, 0, 0,
            0, 0, 1,
            1, 0, 1,
            1, 0, 0,
            //back (y=1)
            0, 1, 0,
            0, 1, 1,
            1, 1, 1,
            1, 1, 0,
            //bottom (z=0)
            0, 0, 0,
            0, 1, 0,
            1, 1, 0,
            1, 0, 0,
            //top (z=1)
            0, 0, 1,
            0, 1, 1,
            1, 1, 1,
            1, 0, 1
        };
    private static final byte[] cubeTex =
        {
            //left
            1, 1,
            1, 0,
            0, 0,
            0, 1,
            //right
            0, 1,
            0, 0,
            1, 0,
            1, 1,
            //front
            0, 1,
            0, 0,
            1, 0,
            1, 1,
            //back
            1, 1,
            1, 0,
            0, 0,
            0, 1,
            //bottom
            0, 0,
            0, 1,
            1, 1,
            1, 0,
            //top
            0, 1,
            0, 0,
            1, 0,
            1, 1
        };
    private static final int[] cubeIndex =
        {
            //left
            0, 1, 2,
            2, 3, 0,
            //right
            6, 5, 4,
            4, 7, 6,
            //front
            10, 9, 8,
            8, 11, 10,
            //back
            12, 13, 14,
            14, 15, 12,
            //bottom
            16, 17, 18,
            18, 19, 16,
            //top
            22, 21, 20,
            20, 23, 22
        };
    
    public ChunkModel(Chunk chunk, int xPos, int yPos, int zPos)
    {
        this.chunk = chunk;
        this.xPos = xPos * Chunk.modelXSize;
        this.yPos = yPos * Chunk.modelYSize;
        this.zPos = zPos * Chunk.modelZSize;
    }
    
    @Override
    @Deprecated
    public void render()
    {
        throw new UnsupportedOperationException("ChunkModel.render() is disabled.");
    }
    
    public int renderBuffers(ByteBuffer vBuffer, IntBuffer iBuffer)
    {
        num = 0;
        
        for (int z = 0; z < Chunk.modelZSize; z++)
        {
            for (int y = 0; y < Chunk.modelYSize; y++)
            {
                for (int x = 0; x < Chunk.modelXSize; x++)
                {
                    num += renderBlock(x + xPos, y + yPos, z + zPos, vBuffer, iBuffer);
                }
            }
        }
        
        vBuffer.flip();
        iBuffer.flip();
        
        return num * cubeIndex.length / 24;
    }
    
    public void renderFinishEmpty()
    {
        if (isFinished && !isDestroyed)
        {
            destroy();
        }
        
        isEmpty = true;
        isFinished = false;
        isDestroyed = false;
        
        absPriority = false;
        basePriority = 1;
    }
    
    public void renderFinish(ByteBuffer vBuffer, IntBuffer iBuffer, int indexCount)
    {
        if (isFinished && !isDestroyed)
        {
            destroy();
        }
        
        this.indexCount = indexCount;
        
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);
        
        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vBuffer, GL_STATIC_DRAW);
        
        glVertexAttribPointer(0, 3, GL_UNSIGNED_BYTE, false, byteSize, 0);
        glVertexAttribPointer(1, 3, GL_UNSIGNED_BYTE, false, byteSize, 3);
        glVertexAttribPointer(2, 1, GL_UNSIGNED_BYTE, false, byteSize, 6);
        
        vboiId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboiId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, iBuffer, GL_STATIC_DRAW);
        
        isFinished = true;
        isEmpty = false;
        isDestroyed = false;
        
        absPriority = false;
        basePriority = 1;
        
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        
        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        
        WorldOfCaves.checkGLErrors("ChunkModel.render()");
    }
    
    private static final boolean[] side = new boolean[6];
    
    private int renderBlock(int x, int y, int z, ByteBuffer vBuffer, IntBuffer iBuffer)
    {
        byte id = chunk.getBlockId(x, y, z);
        
        if (!Block.isVisible(id))
        {
            return 0;
        }
        
        side[0] = !Block.isOpaque(chunk.getBlockId(x-1, y, z));
        side[1] = !Block.isOpaque(chunk.getBlockId(x+1, y, z));
        side[2] = !Block.isOpaque(chunk.getBlockId(x, y-1, z));
        side[3] = !Block.isOpaque(chunk.getBlockId(x, y+1, z));
        side[4] = !Block.isOpaque(chunk.getBlockId(x, y, z-1));
        side[5] = !Block.isOpaque(chunk.getBlockId(x, y, z+1));
        
        byte[] tex = Block.tex(id);
        
        int numAdd = 0;
        int numSub = 0;
        for (int i = 0; i < 6; i++)
        {
            if (side[i])
            {
                for (int j = 0; j < 4; j++)
                {
                    int i2 = (i*4+j)*2;
                    int i3 = (i*4+j)*3;
                    
                    vBuffer.put((byte)(cube[i3] + x));
                    vBuffer.put((byte)(cube[i3+1] + y));
                    vBuffer.put((byte)(cube[i3+2] + z));
                    
                    //vBuffer.put((byte)(16 + tex[i*2] * 16 * 3 + cubeTex[i2] * 16));
                    //vBuffer.put((byte)(16 + tex[i*2+1] * 16 * 3 + cubeTex[i2+1] * 16));
                    vBuffer.put((byte)(cubeTex[i2] * 16));
                    vBuffer.put((byte)(cubeTex[i2+1] * 16));
                    vBuffer.put((byte)tex[i]);
                    
                    vBuffer.put((byte)i);
                }
                
                for (int j = 0; j < 6; j++)
                {
                    iBuffer.put(cubeIndex[i*6+j] + num - numSub);
                }
                
                numAdd += 4;
            }
            else
            {
                numSub += 4;
            }
        }
        
        return numAdd;
    }
    
    public boolean isInFrustum(MatrixHandler mh)
    {
        float x = xPos + chunk.x * Chunk.xSize + Chunk.modelXSize / 2;
        float y = yPos + chunk.y * Chunk.ySize + Chunk.modelYSize / 2;
        float z = zPos + chunk.z * Chunk.zSize + Chunk.modelZSize / 2;
        float size = Chunk.modelXSize / 2;
        
        return mh.cubeInFrustum(x, y, z, size);
    }
    
    public void draw(MatrixHandler mh)
    {
        if (isDestroyed)
        {
            throw new IllegalStateException("Model is already destroyed."); 
        }
        
        if (isEmpty || !isFinished)
        {
            return;
        }
        
        glBindVertexArray(vaoId);
        /*WorldOfCaves.checkGLErrors("ChunkModel.draw(): glBindVertexArray(" + vaoId + ")"
        + "\nisEmpty = " + isEmpty + "\nisFinished = " + isFinished + "\nisDestroyed = " + isDestroyed
        + "\nchunk.x = " + chunk.x + "\nchunk.y = " + chunk.y + "\nchunk.z = " + chunk.z + "\nexists = " + chunk.exists());*/
        //glEnableVertexAttribArray(0);
        //glEnableVertexAttribArray(1);
        //glEnableVertexAttribArray(2);
        //WorldOfCaves.checkGLErrors("ChunkModel.draw(): glEnableVertexAttribArray()");
        
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboiId);
        //WorldOfCaves.checkGLErrors("ChunkModel.draw(): glBindBuffer()");
        
        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
        //WorldOfCaves.checkGLErrors("ChunkModel.draw(): glDrawElements()");
        
        WorldOfCaves.checkGLErrors("ChunkModel.draw()");
    }
    
    @Override
    public void destroy()
    {
        synchronized (destroyLock)
        {
            if (isDestroyed)
            {
                return;
            }
            
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glDeleteBuffers(vboId);
            vboId = -1;

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
            glDeleteBuffers(vboiId);
            vboiId = -1;

            /*glBindVertexArray(vaoId);
            glDisableVertexAttribArray(0);
            glDisableVertexAttribArray(1);
            glDisableVertexAttribArray(2);*/
            glBindVertexArray(0);
            glDeleteVertexArrays(vaoId);
            vaoId = -1;

            isFinished = false;
            isDestroyed = true;

            WorldOfCaves.checkGLErrors("ChunkModel.destroy()");
        }
    }
    
    public boolean isFinished()
    {
        return isFinished;
    }
    
    public boolean isEmpty()
    {
        return isEmpty;
    }
    
    public boolean isDestroyed()
    {
        return isDestroyed;
    }
    
    public void setPriority(float priority)
    {
        basePriority = priority;
        absPriority = false;
    }
    public void setPriority(float priority, boolean abs)
    {
        basePriority = priority;
        absPriority = abs;
    }
    
    public float getPriority()
    {
        if (absPriority)
        {
            return basePriority;
        }
        
        return chunk.getPriority() * basePriority;
    }
}