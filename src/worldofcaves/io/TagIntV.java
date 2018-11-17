package worldofcaves.io;

import java.nio.ByteBuffer;

public class TagIntV extends Tag
{
    public final IntegerV value;
    
    public TagIntV(String name, byte[] value)
    {
        this(name, new IntegerV(value));
    }
    public TagIntV(String name, int value)
    {
        this(name, new IntegerV(value));
    }
    public TagIntV(String name, IntegerV value)
    {
        super(name);
        this.value = value;
    }
    
    @Override
    public short getId()
    {
        return TAG_INTV;
    }
    
    @Override
    public void getData(ByteBuffer buf)
    {
        value.putBuffer(buf);
    }
    
    @Override
    public int getDataSize()
    {
        return value.getSize();
    }
    
    @Override
    public String toString()
    {
        return "[TagIntV name=\"" + name + "\", value=" + value + "]";
    }
}
