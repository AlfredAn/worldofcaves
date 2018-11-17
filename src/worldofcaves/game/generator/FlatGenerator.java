package worldofcaves.game.generator;

import worldofcaves.game.Chunk;
import worldofcaves.game.World;
import worldofcaves.game.block.Block;
import worldofcaves.io.TagByte;
import worldofcaves.io.TagCompound;
import worldofcaves.io.TagInt;

public class FlatGenerator extends WorldGenerator
{
    public final int height;
    public final byte blockType;
    
    public FlatGenerator()
    {
        this(0);
    }
    public FlatGenerator(long seed)
    {
        this(seed, 0, Block.STONE);
    }
    public FlatGenerator(int height, byte blockType)
    {
        this(0, height, blockType);
    }
    public FlatGenerator(long seed, int height, byte blockType)
    {
        super(seed);
        
        this.height = height;
        this.blockType = blockType;
    }
    
    @Override
    public Chunk generate(World world, int chunkX, int chunkY, int chunkZ)
    {
        byte[] blockId = new byte[Chunk.size];
        int i = 0;
        
        for (int z = 0; z < Chunk.zSize; z++)
        {
            for (int y = 0; y < Chunk.ySize; y++)
            {
                for (int x = 0; x < Chunk.xSize; x++)
                {
                    int ax = x + chunkX * Chunk.xSize;
                    int ay = y + chunkY * Chunk.ySize;
                    int az = z + chunkZ * Chunk.zSize;
                    
                    if (z + chunkZ * Chunk.zSize <= height || (ax == 3 && ay > -3 && ay < 3 && az <= 10))
                    {
                        blockId[i] = blockType;
                    }
                    else
                    {
                        blockId[i] = Block.AIR;
                    }
                    
                    i++;
                }
            }
        }
        
        return new Chunk(world, chunkX, chunkY, chunkZ, blockId);
    }
    
    @Override
    public void encode(TagCompound data)
    {
        super.encode(data);
        
        data.add(new TagInt("height", height));
        data.add(new TagByte("blockType", blockType));
    }
    
    @Override
    public short getId()
    {
        return FLAT;
    }
}
