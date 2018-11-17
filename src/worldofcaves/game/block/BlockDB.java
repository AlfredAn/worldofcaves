package worldofcaves.game.block;

import static worldofcaves.game.block.Block.*;

public final class BlockDB
{
    protected static String[] name;
    protected static boolean[] isOpaque, isVisible, isSelectable, isSolid;
    protected static byte[][] tex;
    
    private BlockDB() {}
    
    public static void init()
    {
        name = new String[BLOCK_AMOUNT];
        isOpaque = new boolean[BLOCK_AMOUNT];
        isVisible = new boolean[BLOCK_AMOUNT];
        isSelectable = new boolean[BLOCK_AMOUNT];
        isSolid = new boolean[BLOCK_AMOUNT];
        tex = new byte[BLOCK_AMOUNT][];
        
        block(NONE, "None",
                false, false,
                false, true,
                b(0, 0));
        
        block(AIR, "Air",
                false, false,
                false, false,
                b(0, 0));
        
        block(STONE, "Stone",
                true, true,
                true, true,
                b(1, 0));
        
        block(DIRT, "Dirt",
                true, true,
                true, true,
                b(2, 0));
        
        block(GRASS, "Grass",
                true, true,
                true, true,
                b(4, 0, 4, 0, 4, 0, 4, 0, 2, 0, 3, 0));
    }
    
    private static void block(byte id, String namee,
            boolean opaque, boolean visible,
            boolean selectable, boolean solid,
            byte[] texx)
    {
        name[id] = namee;
        isOpaque[id] = opaque;
        isVisible[id] = visible;
        isSelectable[id] = selectable;
        isSolid[id] = solid;
        tex[id] = texx;
    }
    
    private static byte[] b(int s, int t)
    {
        return b(s, t, s, t, s, t, s, t, s, t, s, t);
    }
    
    private static byte[] b(
            int s1, int t1,
            int s2, int t2,
            int s3, int t3,
            int s4, int t4,
            int s5, int t5,
            int s6, int t6)
    {
        byte[] s = new byte[]
        {
            (byte)s1,
            (byte)s2,
            (byte)s3,
            (byte)s4,
            (byte)s5,
            (byte)s6,
        };
        byte[] t = new byte[]
        {
            (byte)t1,
            (byte)t2,
            (byte)t3,
            (byte)t4,
            (byte)t5,
            (byte)t6
        };
        
        byte[] u = new byte[6];
        for (int i = 0; i < 6; i++)
        {
            u[i] = (byte)(s[i] + t[i] * 16);
        }
        
        byte[] res = new byte[6];
        for (int i = 0; i < 6; i++)
        {
            res[i] = u[i];
        }
        
        return res;
    }
}
