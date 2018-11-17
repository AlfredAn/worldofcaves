package worldofcaves;

public class Vector3i
{
    public int x, y, z;
    
    public Vector3i()
    {
        this(0, 0, 0);
    }
    public Vector3i(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Vector3i)
        {
            Vector3i v = (Vector3i)o;
            return (x == v.x && y == v.y && z == v.z);
        }
        
        return false;
    }

    @Override
    public int hashCode()
    {
        return (x * 257) ^ (y * 2339) ^ (z * 1279);
    }
    
    @Override
    public String toString()
    {
        return "Vector3i[" + x + ", " + y + ", " + z + "]";
    }
}
