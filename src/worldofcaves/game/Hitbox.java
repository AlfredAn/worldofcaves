package worldofcaves.game;

public class Hitbox
{
    public final double
            x1, y1, z1,
            x2, y2, z2,
            xSize, ySize, zSize;
    
    public Hitbox(double x1, double y1, double z1, double x2, double y2, double z2)
    {
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
        
        xSize = x2 - x1;
        ySize = y2 - y1;
        zSize = z2 - z1;
    }
    
    public Hitbox translate(double dx, double dy, double dz)
    {
        return new Hitbox(
                x1 + dx, y1 + dy, z1 + dz,
                x2 + dx, y2 + dy, z2 + dz);
    }
    
    public static boolean collision(Hitbox h1, Hitbox h2,
            double x1, double y1, double z1,
            double x2, double y2, double z2)
    {
        return collision(h1.translate(x1, y1, z1), h2.translate(x2, y2, z2));
    }
    public static boolean collision(Hitbox h1, Hitbox h2)
    {
        return (h1.x1 < h2.x2 && h1.x2 > h2.x1 &&
                h1.y1 < h2.y2 && h1.y2 > h2.y1 &&
                h1.z1 < h2.z2 && h1.z2 > h2.z1);
    }
    
    @Override
    public String toString()
    {
        return "\nx1: " + x1 + "\ny1: " + y1 + "\nz1: " + z1
                 + "\nx2: " + x2 + "\ny2: " + y2 + "\nz2: " + z2;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Hitbox)
        {
            Hitbox h = (Hitbox)o;
            return (h.x1 == x1 && h.y1 == y1 && h.z1 == z1
                 && h.x2 == x2 && h.y2 == y2 && h.z2 == z2);
        }
        
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 89 * hash + (int)(Double.doubleToLongBits(x1) ^ (Double.doubleToLongBits(x1) >>> 32));
        hash = 89 * hash + (int)(Double.doubleToLongBits(y1) ^ (Double.doubleToLongBits(y1) >>> 32));
        hash = 89 * hash + (int)(Double.doubleToLongBits(z1) ^ (Double.doubleToLongBits(z1) >>> 32));
        hash = 89 * hash + (int)(Double.doubleToLongBits(x2) ^ (Double.doubleToLongBits(x2) >>> 32));
        hash = 89 * hash + (int)(Double.doubleToLongBits(y2) ^ (Double.doubleToLongBits(y2) >>> 32));
        hash = 89 * hash + (int)(Double.doubleToLongBits(z2) ^ (Double.doubleToLongBits(z2) >>> 32));
        return hash;
    }
}