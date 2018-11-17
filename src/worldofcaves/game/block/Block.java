package worldofcaves.game.block;

import worldofcaves.game.Hitmask;
import static worldofcaves.game.block.BlockDB.*;

public class Block
{
    public static final byte
            NONE = 0,
            AIR = 1,
            STONE = 2,
            DIRT = 3,
            GRASS = 4;
    public static final byte BLOCK_AMOUNT = 5;
    
    public final int x, y, z;
    public final byte id;
    public final boolean exists;
    
    private Hitmask tHitmask = null;
    
    public Block(byte id)
    {
        this(id, 0, 0, 0, false);
    }
    public Block(byte id, int x, int y, int z)
    {
        this(id, x, y, z, true);
    }
    public Block(byte id, int x, int y, int z, boolean exists)
    {
        this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.exists = exists;
        
        if (id == NONE && exists)
        {
            throw new IllegalArgumentException("BLOCK_NONE cannot have exists == true");
        }
        else if (id < 0 || id >= BLOCK_AMOUNT)
        {
            throw new IllegalArgumentException("Invalid block id: " + id);
        }
    }
    
    public byte getId()
    {
        return id;
    }
    
    public String getName()
    {
        return name[id];
    }
    
    public boolean isOpaque()
    {
        return isOpaque[id];
    }
    
    public boolean isVisible()
    {
        return isVisible[id];
    }
    
    public boolean isSelectable()
    {
        return isSelectable[id];
    }
    
    public boolean isSolid()
    {
        return isSolid[id];
    }
    
    public byte[] tex()
    {
        return tex[id];
    }
    
    public void tex(byte[] dest)
    {
        System.arraycopy(tex[id], 0, dest, 0, 6);
    }
    
    public byte tex(int n)
    {
        return tex[id][n];
    }
    
    public static String getName(byte id)
    {
        return name[id];
    }
    
    public static boolean isOpaque(byte id)
    {
        return isOpaque[id];
    }
    
    public static boolean isVisible(byte id)
    {
        return isVisible[id];
    }
    
    public static boolean isSelectable(byte id)
    {
        return isSelectable[id];
    }
    
    public static boolean isSolid(byte id)
    {
        return isSolid[id];
    }
    
    public static byte[] tex(byte id)
    {
        return tex[id];
    }
    
    public static void tex(byte id, byte[] dest)
    {
        System.arraycopy(tex[id], 0, dest, 0, 6);
    }
    
    public static byte tex(byte id, int n)
    {
        return tex[id][n];
    }
    
    public Hitmask getHitmask()
    {
        if (isSolid())
        {
            return new Hitmask(0, 0, 0, 1, 1, 1);
        }
        
        return new Hitmask();
    }
    
    public Hitmask getTranslatedHitmask()
    {
        if (tHitmask == null)
        {
            tHitmask = getHitmask().translate(x, y, z);
        }
        return tHitmask;
    }
    
    @Override
    public String toString()
    {
        return "\nid: " + getId() + "\nx = " + x + "\ny = " + y + "\nz = " + z + "\nexists = " + exists;
    }
}