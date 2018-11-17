package worldofcaves.io;

import java.nio.ByteBuffer;

public class TagDoubleVec2 extends Tag
{
    public final double x, y;
    
    public TagDoubleVec2(String name, double x, double y)
    {
        super(name);
        this.x = x;
        this.y = y;
    }
    
    @Override
    public short getId()
    {
        return TAG_DOUBLE_VEC2;
    }
    
    @Override
    public void getData(ByteBuffer buf)
    {
        buf.putDouble(x);
        buf.putDouble(y);
    }
    
    @Override
    public int getDataSize()
    {
        return 8 * 2;
    }
    
    @Override
    public String toString()
    {
        return "[TagDoubleVec2 name=\"" + name + "\", value=(" + x + ", " + y + ")]";
    }
}
