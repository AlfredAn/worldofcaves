package worldofcaves.game;

import java.util.ArrayList;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import worldofcaves.MatrixHandler;
import worldofcaves.Shader;
import worldofcaves.Util;
import worldofcaves.Vector3i;
import worldofcaves.model.ChunkModel;
import worldofcaves.game.block.Block;

public final class Chunk implements Comparable<Chunk>
{
    public static final int
            xSize = 32, modelXSize = 32,
            ySize = 32, modelYSize = 32,
            zSize = 32, modelZSize = 32;
    public static final int
            xf = 1,
            yf = xSize,
            zf = xSize * ySize,
            size = xSize * ySize * zSize;
    public static final int
            modelXCount = xSize / modelXSize,
            modelYCount = ySize / modelYSize,
            modelZCount = zSize / modelZSize;
    
    public final int x, y, z;
    public final Vector3i pos;
    public final Vector3f posf;
    //public final FloatBuffer posBuf;
    
    private final boolean exists;
    private byte[] blockId;
    private ChunkModel[][][] model;
    private float priority;
    
    private final Matrix4f matt;
    
    public final World world;
    
    public Chunk(World world, int x, int y, int z)
    {
        this.world = world;
        
        this.x = x;
        this.y = y;
        this.z = z;
        pos = new Vector3i(x, y, z);
        posf = new Vector3f(x * xSize, y * ySize, z * zSize);
        //posBuf = BufferUtils.createFloatBuffer(3);
        //posf.store(posBuf);
        
        matt = MatrixHandler.modelMatrix(posf, MatrixHandler.vec1, MatrixHandler.vec0, null);
        
        exists = false;
        
        updatePriority();
    }
    public Chunk(World world, int x, int y, int z, byte[] blockId)
    {
        this.world = world;
        
        this.x = x;
        this.y = y;
        this.z = z;
        pos = new Vector3i(x, y, z);
        posf = new Vector3f(x * xSize, y * ySize, z * zSize);
        //posBuf = BufferUtils.createFloatBuffer(3);
        //posf.store(posBuf);
        
        matt = MatrixHandler.modelMatrix(posf, MatrixHandler.vec1, MatrixHandler.vec0, null);
        
        this.blockId = blockId;
        
        exists = true;
        
        model = new ChunkModel[modelXCount][modelYCount][modelZCount];
        createModels();
        
        updatePriority();
    }
    
    public void createModels()
    {
        if (!exists)
        {
            throw new IllegalStateException("This chunk doesn't exist.");
        }
        
        for (int zz = 0; zz < modelZCount; zz++)
        {
            for (int yy = 0; yy < modelYCount; yy++)
            {
                for (int xx = 0; xx < modelXCount; xx++)
                {
                    ChunkModel cm = model[xx][yy][zz];
                    
                    if (cm == null)
                    {
                        cm = new ChunkModel(this, xx, yy, zz);
                    }
                    world.chunkUpdate(cm);
                    updateAdjacent(cm);
                    
                    model[xx][yy][zz] = cm;
                }
            }
        }
    }
    
    public void updateAdjacent(ChunkModel cm)
    {
        updateAdjacent(cm, 1f/4096);
    }
    public void updateAdjacent(ChunkModel cm, float prio)
    {
        int bx = x * xSize + cm.xPos;
        int by = y * ySize + cm.yPos;
        int bz = z * zSize + cm.zPos;
        
        ChunkModel cm2;
        
        cm2 = world.getBlockModel(bx - modelXSize, by, bz);
        if (cm2 != null)
        {
            world.chunkUpdate(cm2, prio);
        }
        cm2 = world.getBlockModel(bx + modelXSize, by, bz);
        if (cm2 != null)
        {
            world.chunkUpdate(cm2, prio);
        }
        
        cm2 = world.getBlockModel(bx, by - modelYSize, bz);
        if (cm2 != null)
        {
            world.chunkUpdate(cm2, prio);
        }
        cm2 = world.getBlockModel(bx, by + modelYSize, bz);
        if (cm2 != null)
        {
            world.chunkUpdate(cm2, prio);
        }
        
        cm2 = world.getBlockModel(bx, by, bz - modelZSize);
        if (cm2 != null)
        {
            world.chunkUpdate(cm2, prio);
        }
        cm2 = world.getBlockModel(bx, by, bz + modelZSize);
        if (cm2 != null)
        {
            world.chunkUpdate(cm2, prio);
        }
    }
    
    //private static final FloatBuffer vecBuf = BufferUtils.createFloatBuffer(3);
    
    public void draw(int drawMode, MatrixHandler mh, MatrixHandler mhSun)
    {
        if (!exists)
        {
            throw new IllegalStateException("This chunk doesn't exist.");
        }
        
        boolean mvp = false, mvpu = false;
        
        //MatrixHandler mh = null;
        
        for (int zz = 0; zz < modelZCount; zz++)
        {
            for (int yy = 0; yy < modelYCount; yy++)
            {
                for (int xx = 0; xx < modelXCount; xx++)
                {
                    ChunkModel cm = model[xx][yy][zz];
                    if (cm != null && !cm.isEmpty() && cm.isFinished())
                    {
                        if (!mvp)
                        {
                            //mh = world.game.mat;
                            mh.setModelMatrix(matt);
                            
                            //matSunlight.setView(0, 0, 0, 0, 45, 0);
                            if (mhSun != null)
                            {
                                mhSun.setModelMatrix(matt);
                            }
                            mvp = true;
                        }
                        
                        if (cm.isInFrustum(mh))
                        {
                            if (!mvpu)
                            {
                                if (drawMode == Game.DRAW_ACTUAL)
                                {
                                    mh.uniformMVP(Shader.block.getUniformLocation(0));
                                    if (mhSun != null)
                                    {
                                        mhSun.uniformMVP(Shader.block.getUniformLocation(3), true);
                                    }
                                    
                                    //mh.uniform(Shader.block.getUniformLocation(2), mh.getModel());
                                }
                                else if (drawMode == Game.DRAW_LIGHTMAP)
                                {
                                    mh.uniformMVP(Shader.lightmap.getUniformLocation(0));
                                }
                                //posf.store(vecBuf);
                                //vecBuf.flip();
                                //glUniform3(Shader.block.getUniformLocation(1), vecBuf);
                                mvpu = true;
                            }
                            
                            cm.draw(mh);
                        }
                    }
                }
            }
        }
    }
    
    public void destroy()
    {
        if (!exists)
        {
            return;
        }
        
        for (int zz = 0; zz < modelZCount; zz++)
        {
            for (int yy = 0; yy < modelYCount; yy++)
            {
                for (int xx = 0; xx < modelXCount; xx++)
                {
                    model[xx][yy][zz].destroy();
                }
            }
        }
    }
    
    public void setBlock(Block b)
    {
        setBlock(b, 1);
    }
    public void setBlock(Block b, float prio)
    {
        setBlock(b, prio, false);
    }
    public void setBlock(Block b, float prio, boolean invertPrio)
    {
        if (!exists)
        {
            throw new IllegalStateException("This chunk doesn't exist.");
        }
        
        int bx = b.x - x * xSize;
        int by = b.y - y * ySize;
        int bz = b.z - z * zSize;
        
        if (b.getId() == Block.NONE)
        {
            throw new IllegalArgumentException("Cannot set block to BLOCK_NONE.");
        }
        else if (bx < 0 || by < 0 || bz < 0
                || bx >= xSize || by >= ySize || bz >= zSize)
        {
            throw new IllegalArgumentException("Block coordinates out of range: "  + "\nx: " + bx + "\ny: " + by + "\nz: " + bz);
        }
        else
        {
            setId(bx, by, bz, b.getId());
        }
        
        world.chunkUpdate(getAdjacentBlockModels(bx, by, bz), prio, false, invertPrio);
    }
    
    private ArrayList<ChunkModel> getAdjacentBlockModels(int x, int y, int z)
    {
        if (!exists)
        {
            throw new IllegalStateException("This chunk doesn't exist.");
        }
        
        ArrayList<ChunkModel> models = new ArrayList<>();
        
        int bx = x + this.x * xSize;
        int by = y + this.y * ySize;
        int bz = z + this.z * zSize;
        
        models.add(getBlockModel(x, y, z));
        ChunkModel cm;
        
        if (x % modelXSize == 0)
        {
            cm = world.getBlockModel(bx - modelXSize, by, bz);
            if (cm != null) models.add(cm);
        }
        else if (x % modelXSize == modelXSize - 1)
        {
            cm = world.getBlockModel(bx + modelXSize, by, bz);
            if (cm != null) models.add(cm);
        }
        
        if (y % modelYSize == 0)
        {
            cm = world.getBlockModel(bx, by - modelYSize, bz);
            if (cm != null) models.add(cm);
        }
        else if (y % modelYSize == modelYSize - 1)
        {
            cm = world.getBlockModel(bx, by + modelYSize, bz);
            if (cm != null) models.add(cm);
        }
        
        if (z % modelZSize == 0)
        {
            cm = world.getBlockModel(bx, by, bz - modelZSize);
            if (cm != null) models.add(cm);
        }
        else if (z % modelZSize == modelZSize - 1)
        {
            cm = world.getBlockModel(bx, by, bz + modelZSize);
            if (cm != null) models.add(cm);
        }
        
        return models;
    }
    
    public ChunkModel getBlockModel(int x, int y, int z)
    {
        if (!exists)
        {
            throw new IllegalStateException("This chunk doesn't exist.");
        }
        
        int mx = x / modelXSize;
        int my = y / modelYSize;
        int mz = z / modelZSize;
        
        if (mx < 0 || mx >= modelXCount
         || my < 0 || my >= modelYCount
         || mz < 0 || mz >= modelZCount)
        {
            throw new IllegalArgumentException("Block position out of bounds: " + Util.coordString(x, y, z));
        }
        
        return model[mx][my][mz];
    }
    
    public byte getBlockId(int x, int y, int z)
    {
        if (!exists)
        {
            throw new IllegalStateException("This chunk doesn't exist.");
        }
        
        if (x < 0 || y < 0 || z < 0
                || x >= xSize || y >= ySize || z >= zSize)
        {
            return world.getBlockId(x + this.x * xSize, y + this.y * ySize, z + this.z * zSize);
        }
        
        return getId(x, y, z);
    }
    
    public Block getBlock(int x, int y, int z)
    {
        if (!exists)
        {
            throw new IllegalStateException("This chunk doesn't exist.");
        }
        
        if (x < 0 || y < 0 || z < 0
                || x >= xSize || y >= ySize || z >= zSize)
        {
            return world.getBlock(x + this.x * xSize, y + this.y * ySize, z + this.z * zSize);
        }
        
        return new Block(getId(x, y, z), x + this.x * xSize, y + this.y * ySize, z + this.z * zSize);
    }
    
    private void setId(int x, int y, int z, byte id)
    {
        blockId[x + y*xSize + z*xSize*ySize] = id;
    }
    
    private byte getId(int x, int y, int z)
    {
        return blockId[x + y*xSize + z*xSize*ySize];
    }
    
    public boolean isInFrustum(MatrixHandler mh)
    {
        float xx = x * xSize + xSize / 2;
        float yy = y * ySize + ySize / 2;
        float zz = z * zSize + zSize / 2;
        float size2 = xSize / 2;
        
        return mh.cubeInFrustum(xx, yy, zz, size2);
    }
    
    public void updatePriority()
    {
        if (exists)
        {
            priority = world.getChunkLoadPriority(this);
        }
        else
        {
            priority = 0;
        }
    }
    
    public ChunkModel getModel()
    {
        return model[0][0][0];
    }
    
    public float getPriority()
    {
        return priority;
    }
    
    public boolean exists()
    {
        return exists;
    }
    
    @Override
    public String toString()
    {
        return "Chunk[" + x + ", " + y + ", " + z + "]";
    }
    
    @Override
    public int compareTo(Chunk c)
    {
        float prio = c.getPriority();
        return -Float.compare(priority, prio);
    }
}