package worldofcaves.io;

import java.nio.ByteBuffer;

public class TagLong extends Tag
{
    public final long value;
    
    public TagLong(String name, long value)
    {
        super(name);
        this.value = value;
    }
    
    @Override
    public short getId()
    {
        return TAG_LONG;
    }
    
    @Override
    public void getData(ByteBuffer buf)
    {
        buf.putLong(value);
    }
    
    @Override
    public int getDataSize()
    {
        return 8;
    }
    
    @Override
    public String toString()
    {
        return "[TagLong name=\"" + name + "\", value=" + value + "]";
    }
}
