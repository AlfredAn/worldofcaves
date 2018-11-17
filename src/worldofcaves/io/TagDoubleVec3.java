package worldofcaves.io;

import java.nio.ByteBuffer;

public class TagDoubleVec3 extends Tag
{
    public final double x, y, z;
    
    public TagDoubleVec3(String name, double x, double y, double z)
    {
        super(name);
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    @Override
    public short getId()
    {
        return TAG_DOUBLE_VEC3;
    }
    
    @Override
    public void getData(ByteBuffer buf)
    {
        buf.putDouble(x);
        buf.putDouble(y);
        buf.putDouble(z);
    }
    
    @Override
    public int getDataSize()
    {
        return 8 * 3;
    }
    
    @Override
    public String toString()
    {
        return "[TagDoubleVec3 name=\"" + name + "\", value=(" + x + ", " + y + ", " + z + ")]";
    }
}
