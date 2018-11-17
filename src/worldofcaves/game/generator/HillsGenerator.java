package worldofcaves.game.generator;

import java.util.Random;
import worldofcaves.game.Chunk;
import worldofcaves.game.World;
import worldofcaves.game.block.Block;

public class HillsGenerator extends WorldGenerator
{
    private final SimplexNoise baseHeight, plainsHeight, hillsHeight, mountainsHeight,
            biomeTemp, biomeMoist, biomeHeight,
            soilDepth;//, cave1, cave2;//, transX, transY;
    //private final float[][]
            /*terrainArray = new float[Chunk.xSize][Chunk.ySize],;/*,
            transXArray = new float[Chunk.xSize][Chunk.ySize],
            transYArray = new float[Chunk.xSize][Chunk.ySize];*/
    
    public HillsGenerator()
    {
        this(0);
    }
    public HillsGenerator(long seed)
    {
        super(seed);
        
        Random random = new Random(this.seed);
        baseHeight = new SimplexNoise(random.nextLong(), 5, .5f, 256, 24, 24);
        
        plainsHeight = new SimplexNoise(random.nextLong(), 5, .6f, 16, 4, 4);
        hillsHeight = new SimplexNoise(random.nextLong(), 6, .5f, 8, 24, 12);
        mountainsHeight = new SimplexNoise(random.nextLong(), 10, .55f, 2, 128, 48);
        
        biomeTemp = new SimplexNoise(random.nextLong(), 3, .4f, 128, 1);
        biomeMoist = new SimplexNoise(random.nextLong(), 3, .4f, 128, 1);
        biomeHeight = new SimplexNoise(random.nextLong(), 4, .3f, 128, 1.15f);
        
        soilDepth = new SimplexNoise(random.nextLong(), 2, .7f, 8, 1.5f, 3f);
        //transX = new SimplexNoise(64, .3, random.nextLong());
        //transY = new SimplexNoise(64, .3, random.nextLong());
        //cave1 = new SimplexNoise(random.nextLong(), 6, .6f, 1, 1);
        //cave2 = new SimplexNoise(random.nextLong(), 6, .6f, 1, 1);
    }
    
    @Override
    public short getId()
    {
        return HILLS;
    }
    
    @Override
    public Chunk generate(World world, int chunkX, int chunkY, int chunkZ)
    {
        byte[] blockId = new byte[Chunk.size];
        
        //terrainHeight.genArray(chunkX * Chunk.xSize, chunkY * Chunk.ySize, terrainArray);
        int xs = 1, ys = Chunk.xSize, zs = ys * Chunk.ySize;
        
        for (int x = 0; x < Chunk.xSize; x++)
        {
            int ax = x + chunkX * Chunk.xSize;
            
            for (int y = 0; y < Chunk.ySize; y++)
            {
                int ay = y + chunkY * Chunk.ySize;
                
                //int h = (int)(terrainHeight.getNoise(ax, ay));
                float temp = biomeTemp.getNoise(ax, ay);
                float moist = biomeMoist.getNoise(ax, ay);
                float height = biomeHeight.getNoise(ax, ay);
                
                byte biome = getBiome(temp, moist, height);
                float fh = baseHeight.getNoise(ax, ay) + getBiomeHeight(temp, moist, height, ax, ay);
                int sh = (int)(fh - soilDepth.getNoise(ax, ay));
                int h = (int)fh;
                
                for (int z = 0; z < Chunk.zSize; z++)
                {
                    int az = z + chunkZ * Chunk.zSize;
                    int i = x + y * ys + z * zs;
                    
                    if (az >= h)
                    {
                        blockId[i] = Block.AIR;
                    }
                    else
                    {
                        //float dens1 = (float)cave1.getNoise(ax/4, ay/4, az/2);
                        //float dens2 = (float)cave2.getNoise(ax/4, ay/4, az/2);
                        
                        if (false)//(dens1 > .7)
                        {
                            blockId[i] = Block.AIR;
                        }
                        else if (az >= h - 1)
                        {
                            blockId[i] = Block.GRASS;
                        }
                        else if (az >= sh)
                        {
                            blockId[i] = Block.DIRT;
                        }
                        else
                        {
                            blockId[i] = Block.STONE;
                        }
                    }
                }
            }
        }
        
        return new Chunk(world, chunkX, chunkY, chunkZ, blockId);
    }
    
    private static final byte BIOME_PLAINS = 0, BIOME_HILLS = 1, BIOME_MOUNTAINS = 2;
    
    private byte getBiome(float temp, float moist, float height)
    {
        if (height > 1f/3)
        {
            return BIOME_MOUNTAINS;
        }
        else if (height > 0)
        {
            return BIOME_HILLS;
        }
        else
        {
            return BIOME_PLAINS;
        }
        
        //throw new IllegalArgumentException();
    }
    
    private float getBiomeHeight(float temp, float moist, float height, int ax, int ay)
    {
        if (height >= 0.5f)
        {
            return mountainsHeight.getNoise(ax, ay);
        }
        else if (height > 0.375f)
        {
            return cosInterpolate(
                    hillsHeight.getNoise(ax, ay),
                    mountainsHeight.getNoise(ax, ay),
                    height, 0.375f, 0.5f);
        }
        else if (height >= 0f)
        {
            return hillsHeight.getNoise(ax, ay);
        }
        else if (height > -0.125f)
        {
            return cosInterpolate(
                    plainsHeight.getNoise(ax, ay),
                    hillsHeight.getNoise(ax, ay),
                    height, -0.125f, 0f);
        }
        else
        {
            return plainsHeight.getNoise(ax, ay);
        }
        
        //throw new IllegalArgumentException();
    }
    
    private float interpolate(float a, float b, float f, float min, float max)
    {
        float d = max - min;
        float af = (f - min) / d;
        
        return a * (1 - af) + b * af;
    }
    
    private float interpolate(float a, float b, float f)
    {
        return a * (1 - f) + b * f;
    }
    
    private float cosInterpolate(float a, float b, float f, float min, float max)
    {
        float d = max - min;
        float af = (f - min) / d;
        
        float ff = (float)(1 - Math.cos(Math.PI * af)) * 0.5f;
        return (a * (1 - ff)) + b * ff;
    }
    
    private float cosInterpolate(float a, float b, float f)
    {
        float ff = (float)(1 - Math.cos(Math.PI * f)) * 0.5f;
        return (a * (1 - ff)) + b * ff;
    }
}