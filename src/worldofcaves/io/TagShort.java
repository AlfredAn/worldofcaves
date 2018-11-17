package worldofcaves.io;

import java.nio.ByteBuffer;

public class TagShort extends Tag
{
    public final short value;
    
    public TagShort(String name, short value)
    {
        super(name);
        this.value = value;
    }
    
    @Override
    public short getId()
    {
        return TAG_SHORT;
    }
    
    @Override
    public void getData(ByteBuffer buf)
    {
        buf.putShort(value);
    }
    
    @Override
    public int getDataSize()
    {
        return 2;
    }
    
    @Override
    public String toString()
    {
        return "[TagShort name=\"" + name + "\", value=" + value + "]";
    }
}
