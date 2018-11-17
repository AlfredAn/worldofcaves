package worldofcaves.io;

import java.nio.ByteBuffer;

public class TagString extends Tag
{
    public final String value;
    
    public TagString(String name, String value)
    {
        super(name);
        this.value = value;
    }
    
    @Override
    public short getId()
    {
        return TAG_STRING;
    }
    
    @Override
    public void getData(ByteBuffer buf)
    {
        Tag.putString(value, buf);
    }
    
    @Override
    public int getDataSize()
    {
        return Tag.stringSize(value);
    }
    
    @Override
    public String toString()
    {
        return "[TagString name=\"" + name + "\", value=\"" + value + "\"]";
    }
}
