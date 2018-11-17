package worldofcaves.game;

import java.util.ArrayList;
import worldofcaves.game.block.Block;

public class Hitmask
{
    private final ArrayList<Hitbox> hitbox = new ArrayList<>(1);
    private boolean isUpdated = false;
    private double
            x1, y1, z1,
            x2, y2, z2,
            xSize, ySize, zSize;
    
    public Hitmask() {}
    public Hitmask(double x1, double y1, double z1, double x2, double y2, double z2)
    {
        hitbox.add(new Hitbox(x1, y1, z1, x2, y2, z2));
    }
    public Hitmask(Hitbox h)
    {
        hitbox.add(h);
    }
    
    public void addHitbox(Hitbox h)
    {
        hitbox.add(h);
        isUpdated = false;
    }
    
    public int getAmount()
    {
        return hitbox.size();
    }
    
    public Hitbox getHitbox(int i)
    {
        return hitbox.get(i);
    }
    
    public Hitmask translate(double dx, double dy, double dz)
    {
        Hitmask h = new Hitmask();
        
        for (int i = 0; i < hitbox.size(); i++)
        {
            h.addHitbox(hitbox.get(i).translate(dx, dy, dz));
        }
        
        return h;
    }
    
    public static boolean collision(Hitmask h1, Hitbox h2)
    {
        return collision(h1, new Hitmask(h2));
    }
    public static boolean collision(Hitmask h1, Hitbox h2,
            double x1, double y1, double z1,
            double x2, double y2, double z2)
    {
        return collision(h1.translate(x1, y1, z1), new Hitmask(h2.translate(x2, y2, z2)));
    }
    public static boolean collision(Hitmask h1, Hitmask h2,
            double x1, double y1, double z1,
            double x2, double y2, double z2)
    {
        return collision(h1.translate(x1, y1, z1), h2.translate(x2, y2, z2));
    }
    public static boolean collision(Hitmask h1, Hitmask h2)
    {
        for (int i1 = 0; i1 < h1.getAmount(); i1++)
        {
            for (int i2 = 0; i2 < h2.getAmount(); i2++)
            {
                if (Hitbox.collision(h1.getHitbox(i1), h2.getHitbox(i2)))
                {
                    return true;
                }
            }
        }
        
        return false;
    }
    public static ArrayList<Hitbox> collisionList(Hitmask h1, Hitmask h2)
    {
        ArrayList<Hitbox> cList = new ArrayList<>();
        
        for (int i1 = 0; i1 < h1.getAmount(); i1++)
        {
            for (int i2 = 0; i2 < h2.getAmount(); i2++)
            {
                if (Hitbox.collision(h1.getHitbox(i1), h2.getHitbox(i2)))
                {
                    cList.add(h2.getHitbox(i2));
                }
            }
        }
        
        return cList;
    }
    
    public ArrayList<Hitbox> worldCollisionList(World world)
    {
        return worldCollisionList(world, 0, 0, 0);
    }
    public ArrayList<Hitbox> worldCollisionList(World world, double x, double y, double z)
    {
        if (!isUpdated)
        {
            updateVars();
        }
        
        int xMin = (int)(x + x1 - 2);
        int yMin = (int)(y + y1 - 2);
        int zMin = (int)(z + z1 - 2);
        int xMax = (int)(x + x2 + 3);
        int yMax = (int)(y + y2 + 3);
        int zMax = (int)(z + z2 + 3);
        
        ArrayList<Hitbox> cList = new ArrayList<>();
        
        Hitmask h = translate(x, y, z);
        
        for (int zz = zMin; zz <= zMax; zz++)
        {
            for (int yy = yMin; yy <= yMax; yy++)
            {
                for (int xx = xMin; xx <= xMax; xx++)
                {
                    Block b = world.getBlock(xx, yy, zz);
                    
                    if (b.isSolid())
                    {
                        Hitmask bm = b.getTranslatedHitmask();
                        ArrayList<Hitbox> cList2 = collisionList(h, bm);
                        
                        if (cList2.size() > 0)
                        {
                            cList.addAll(cList2);
                        }
                    }
                }
            }
        }
        
        return cList;
    }
    
    private void updateVars()
    {
        if (isUpdated)
        {
            return;
        }
        
        for (int i = 0; i < hitbox.size(); i++)
        {
            Hitbox h = hitbox.get(i);
            
            if (i == 0 || h.x1 < x1)
            {
                x1 = h.x1;
            }
            if (i == 0 || h.y1 < y1)
            {
                y1 = h.y1;
            }
            if (i == 0 || h.z1 < z1)
            {
                z1 = h.z1;
            }
            if (i == 0 || h.x2 > x2)
            {
                x2 = h.x2;
            }
            if (i == 0 || h.y2 > y2)
            {
                y2 = h.y2;
            }
            if (i == 0 || h.z2 > z2)
            {
                z2 = h.z2;
            }
        }
        
        xSize = x2 - x1;
        ySize = y2 - y1;
        zSize = z2 - z1;
        isUpdated = true;
    }
    
    public double getX1()
    {
        if (!isUpdated)
        {
            updateVars();
        }
        
        return x1;
    }
    
    public double getY1()
    {
        if (!isUpdated)
        {
            updateVars();
        }
        
        return y1;
    }
    
    public double getZ1()
    {
        if (!isUpdated)
        {
            updateVars();
        }
        
        return z1;
    }
    
    public double getX2()
    {
        if (!isUpdated)
        {
            updateVars();
        }
        
        return x2;
    }
    
    public double getY2()
    {
        if (!isUpdated)
        {
            updateVars();
        }
        
        return y2;
    }
    
    public double getZ2()
    {
        if (!isUpdated)
        {
            updateVars();
        }
        
        return z2;
    }
    
    public double getXSize()
    {
        if (!isUpdated)
        {
            updateVars();
        }
        
        return xSize;
    }
    
    public double getYSize()
    {
        if (!isUpdated)
        {
            updateVars();
        }
        
        return ySize;
    }
    
    public double getZSize()
    {
        if (!isUpdated)
        {
            updateVars();
        }
        
        return zSize;
    }
}
