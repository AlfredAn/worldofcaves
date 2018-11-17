package worldofcaves.game.generator;

import worldofcaves.Util;
import worldofcaves.Vector3i;
import worldofcaves.game.Chunk;
import worldofcaves.game.World;
import worldofcaves.game.block.Block;
import worldofcaves.io.TagByte;
import worldofcaves.io.TagCompound;
import worldofcaves.io.TagFloat;
import worldofcaves.io.TagInt;

public class BottomlessHoleGenerator extends WorldGenerator
{
    public final int height;
    public final byte blockType;
    public final float holeSize, platformSize;
    private final float hs, ps;
    
    public BottomlessHoleGenerator()
    {
        this(0);
    }
    public BottomlessHoleGenerator(long seed)
    {
        this(seed, 0, Block.STONE);
    }
    public BottomlessHoleGenerator(int height, byte blockType)
    {
        this(0, height, blockType);
    }
    public BottomlessHoleGenerator(long seed, int height, byte blockType)
    {
        this(seed, height, blockType, 64, 8);
    }
    public BottomlessHoleGenerator(long seed, int height, byte blockType, float holeSize, float platformSize)
    {
        super(seed);
        
        this.height = height;
        this.blockType = blockType;
        this.holeSize = holeSize;
        this.platformSize = platformSize;
        hs = holeSize * holeSize;
        ps = platformSize * platformSize;
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
                    Vector3i pos = getWorldCoords(x, y, z, chunkX, chunkY, chunkZ);
                    
                    float d = Util.originDistanceSqr(pos.x, pos.y);
                    if (z + chunkZ * Chunk.zSize <= height && (d > hs || (d < ps && pos.z >= -2)))
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
        data.add(new TagFloat("holeSize", holeSize));
        data.add(new TagFloat("platformSize", platformSize));
    }
    
    @Override
    public short getId()
    {
        return BOTTOMLESS_HOLE;
    }
}
