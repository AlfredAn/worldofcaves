package worldofcaves.io;

import java.nio.ByteBuffer;

public class TagInt extends Tag
{
    public final int value;
    
    public TagInt(String name, int value)
    {
        super(name);
        this.value = value;
    }
    
    @Override
    public short getId()
    {
        return TAG_INT;
    }
    
    @Override
    public void getData(ByteBuffer buf)
    {
        buf.putInt(value);
    }
    
    @Override
    public int getDataSize()
    {
        return 4;
    }
    
    @Override
    public String toString()
    {
        return "[TagInt name=\"" + name + "\", value=" + value + "]";
    }
}
