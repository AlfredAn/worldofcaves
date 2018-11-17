package worldofcaves.game.generator;

import java.util.Random;

public class SimplexNoise
{
    private final long seed;
    private final int octaves;
    private final float persistence, scale, size, offset;
    private final SimplexNoiseOctave[] oct;
    private final float[] freq, amp;
    
    public SimplexNoise(long seed, int octaves, float persistence)
    {
        this(seed, octaves, persistence, 1, 1, 0);
    }
    public SimplexNoise(long seed, int octaves, float persistence, float scale, float size)
    {
        this(seed, octaves, persistence, scale, size, 0);
    }
    public SimplexNoise(long seed, int octaves, float persistence, float scale, float size, float offset)
    {
        this.seed = seed;
        this.octaves = octaves;
        this.persistence = persistence;
        this.scale = scale;
        this.size = size;
        this.offset = offset;
        
        oct = new SimplexNoiseOctave[octaves];
        freq = new float[octaves];
        amp = new float[octaves];
        
        Random rand = new Random(seed);
        
        for (int i = 0; i < octaves; i++)
        {
            oct[i] = new SimplexNoiseOctave(rand.nextLong());
            freq[i] = (float)(Math.pow(2, i) * scale);
            amp[i] = (float)(Math.pow(persistence, octaves - i) * size);
        }
    }
    
    public float getNoise(double x, double y)
    {
        float r = offset;
        
        for (int i = 0; i < octaves; i++)
        {
            r += oct[i].noise(x / freq[i] + 9845, y / freq[i] + 63578) * amp[i];
        }
        
        return r;
    }
    
    public float getNoise(double x, double y, double z)
    {
        float r = offset;
        
        for (int i = 0; i < octaves; i++)
        {
            r += oct[i].noise(x / freq[i] + 9845, y / freq[i] + 63578, z / freq[i] - 69314) * amp[i];
        }
        
        return r;
    }
    
    public float getNoise(double x, double y, double z, double w)
    {
        float r = offset;
        
        for (int i = 0; i < octaves; i++)
        {
            r += oct[i].noise(x / freq[i] + 9845, y / freq[i] + 63578, z / freq[i] - 69314, w / freq[i] - 635) * amp[i];
        }
        
        return r;
    }
    
    public float[][] genArray(int xOff, int yOff, float[][] dest)
    {
        int xs = dest.length;
        int ys = dest[0].length;
        
        for (int y = 0; y < ys; y++)
        {
            for (int x = 0; x < xs; x++)
            {
                dest[x][y] = (float)getNoise(x + xOff, y + yOff);
            }
        }
        
        return dest;
    }
} 