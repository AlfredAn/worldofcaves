package worldofcaves.io;

import java.nio.ByteBuffer;

public class TagFloat extends Tag
{
    public final float value;
    
    public TagFloat(String name, float value)
    {
        super(name);
        this.value = value;
    }
    
    @Override
    public short getId()
    {
        return TAG_FLOAT;
    }
    
    @Override
    public void getData(ByteBuffer buf)
    {
        buf.putFloat(value);
    }
    
    @Override
    public int getDataSize()
    {
        return 4;
    }
    
    @Override
    public String toString()
    {
        return "[TagFloat name=\"" + name + "\", value=" + value + "]";
    }
}
