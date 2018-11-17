package worldofcaves.io;

import java.nio.ByteBuffer;

public class TagByte extends Tag
{
    public final byte value;
    
    public TagByte(String name, byte value)
    {
        super(name);
        this.value = value;
    }
    
    @Override
    public short getId()
    {
        return TAG_BYTE;
    }
    
    @Override
    public void getData(ByteBuffer buf)
    {
        buf.put(value);
    }
    
    @Override
    public int getDataSize()
    {
        return 1;
    }
    
    @Override
    public String toString()
    {
        return "[TagByte name=\"" + name + "\", value=" + value + "]";
    }
}
