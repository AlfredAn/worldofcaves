package worldofcaves.io;

import java.nio.ByteBuffer;

public class TagDouble extends Tag
{
    public final double value;
    
    public TagDouble(String name, double value)
    {
        super(name);
        this.value = value;
    }
    
    @Override
    public short getId()
    {
        return TAG_DOUBLE;
    }
    
    @Override
    public void getData(ByteBuffer buf)
    {
        buf.putDouble(value);
    }
    
    @Override
    public int getDataSize()
    {
        return 8;
    }
    
    @Override
    public String toString()
    {
        return "[TagDouble name=\"" + name + "\", value=" + value + "]";
    }
}
