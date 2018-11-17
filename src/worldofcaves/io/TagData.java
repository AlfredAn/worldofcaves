package worldofcaves.io;

import java.nio.ByteBuffer;
import worldofcaves.Util;

public class TagData extends Tag
{
    public final ByteBuffer data;
    private final IntegerV size;
    
    public TagData(String name, byte[] data)
    {
        this(name, (ByteBuffer)(ByteBuffer.allocateDirect(data.length).order(Tag.byteOrder).put(data)).flip(), false);
    }
    public TagData(String name, ByteBuffer data)
    {
        this(name, (ByteBuffer)(ByteBuffer.allocateDirect(data.limit()).order(Tag.byteOrder).put(data)).flip(), false);
    }
    private TagData(String name, ByteBuffer data, boolean foo)
    {
        super(name);
        
        this.data = data;
        
        if (data.limit() > IntegerV.MAX_VALUE)
        {
            throw new IllegalArgumentException(
                    "Data is too long; maximum allowed size is "
                    + IntegerV.MAX_VALUE + " bytes.");
        }
        
        size = new IntegerV(data.limit());
    }
    
    @Override
    public short getId()
    {
        return TAG_DATA;
    }
    
    @Override
    public void getData(ByteBuffer buf)
    {
        size.putBuffer(buf);
        buf.put(data);
    }
    
    @Override
    public int getDataSize()
    {
        return size.getSize() + data.limit();
    }
    
    @Override
    public String toString()
    {
        byte[] bytes = new byte[data.limit()];
        data.get(bytes);
        
        return "[TagData name=\"" + name + "\", data=\"" + Util.bytesToHex(bytes) + "\"]";
    }
}
