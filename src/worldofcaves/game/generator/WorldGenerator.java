package worldofcaves.game.generator;

import java.io.IOException;
import worldofcaves.Util;
import worldofcaves.Vector3i;
import worldofcaves.WorldOfCaves;
import worldofcaves.game.Chunk;
import worldofcaves.game.World;
import worldofcaves.game.block.Block;
import worldofcaves.io.Enumerable;
import worldofcaves.io.TagFormatException;
import worldofcaves.io.Encodable;
import worldofcaves.io.TagCompound;
import worldofcaves.io.TagLong;

public abstract class WorldGenerator implements Encodable, Enumerable
{
    public static final short
            HILLS = 0,
            FLAT = 1,
            ASYMPTOTE_NOISE = 2,
            BOTTOMLESS_HOLE = 3;
    
    public final long seed;
    
    public WorldGenerator()
    {
        this(0);
    }
    /**
     *
     * @param seed if 0, a random seed will be used
     */
    public WorldGenerator(long seed)
    {
        while (seed == 0)
        {
            seed = Util.random.nextLong();
        }
        
        this.seed = seed;
    }
    
    public abstract Chunk generate(World world, int chunkX, int chunkY, int chunkZ);
    
    public Vector3i getWorldCoords(int x, int y, int z, int chunkX, int chunkY, int chunkZ)
    {
        return new Vector3i(x + chunkX * Chunk.xSize, y + chunkY * Chunk.ySize, z + chunkZ * Chunk.zSize);
    }
    
    @Override
    public void encode(TagCompound data)
    {
        data.add(new TagLong("seed", seed));
    }
    
    public static WorldGenerator decode(TagCompound data) throws TagFormatException
    {
        int id = data.findIntVAsInt("id", -1);
        
        if (id == -1)
        {
            throw new TagFormatException("Error loading world generator: Missing id tag");
        }
        
        long seed = data.findLong("seed", 0);
        
        if (seed == 0)
        {
            WorldOfCaves.logError("Warning: Missing seed tag in WorldGenerator.");
        }
        
        switch (id)
        {
            case HILLS:
                return new HillsGenerator(seed);
            case FLAT:
            case ASYMPTOTE_NOISE:
            case BOTTOMLESS_HOLE:
                int height = data.findInt("height", Integer.MIN_VALUE);
                byte blockType = data.findByte("blockType", Block.NONE);
                
                if (height == Integer.MIN_VALUE)
                {
                    throw new TagFormatException("Error loading world generator: Missing height tag");
                }
                
                if (blockType == Block.NONE)
                {
                    throw new TagFormatException("Error loading world generator: Missing blockType tag");
                }
                
                switch (id)
                {
                    case FLAT:
                        return new FlatGenerator(seed, height, blockType);
                    case ASYMPTOTE_NOISE:
                        return new AsymptoteNoiseGenerator(seed, height, blockType);
                    case BOTTOMLESS_HOLE:
                        float holeSize = data.findFloat("holeSize", Float.NaN);
                        float platformSize = data.findFloat("platformSize", Float.NaN);
                        
                        if (holeSize == Float.NaN)
                        {
                            throw new TagFormatException("Error loading world generator: Missing holeSize tag");
                        }
                        
                        if (platformSize == Float.NaN)
                        {
                            throw new TagFormatException("Error loading world generator: Missing platformSize tag");
                        }
                        
                        return new BottomlessHoleGenerator(seed, height, blockType, holeSize, platformSize);
                }
        }
        
        throw new TagFormatException("Error loading world generator: Invalid id " + id);
    }
}