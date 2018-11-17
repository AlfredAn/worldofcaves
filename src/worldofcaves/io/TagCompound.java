package worldofcaves.io;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import worldofcaves.Util;

public class TagCompound extends Tag
{
    private final ArrayList<Tag> tags;
    
    public TagCompound(String name)
    {
        this(name, 10);
    }
    public TagCompound(String name, int initialCapacity)
    {
        super(name);
        tags = new ArrayList<>(initialCapacity);
    }
    
    @Override
    public short getId()
    {
        return TAG_COMPOUND;
    }
    
    @Override
    public void getData(ByteBuffer buf)
    {
        for (int i = 0; i < tags.size(); i++)
        {
            tags.get(i).bufferData(buf);
        }
        
        buf.put(TAG_END);
    }
    
    @Override
    public int getDataSize()
    {
        int size = 1;
        
        for (int i = 0; i < tags.size(); i++)
        {
            size += tags.get(i).getSize();
        }
        
        return size;
    }
    
    public void add(Tag tag)
    {
        if (tag == null)
        {
            throw new NullPointerException("Compound tags cannot contain null elements.");
        }
        
        for (int i = 0; i < tags.size(); i++)
        {
            if (tags.get(i).name.equals(tag.name))
            {
                throw new IllegalStateException("Two tags cannot have the same name.");
            }
        }
        
        tags.add(tag);
    }
    
    public boolean remove(Tag tag)
    {
        if (tag == null)
        {
            return false;
        }
        
        return tags.remove(tag);
    }
    
    public Tag get(int i)
    {
        return tags.get(i);
    }
    
    public Tag find(String name)
    {
        for (int i = 0; i < tags.size(); i++)
        {
            Tag t = tags.get(i);
            if (t.name.equals(name))
            {
                return t;
            }
        }
        
        return null;
    }
    
    public Tag findType(String name, short id)
    {
        Tag t = find(name);
        
        if (t != null && t.getId() == id)
        {
            return t;
        }
        
        return null;
    }
    
    public TagCompound findCompoundTag(String name)
    {
        return (TagCompound)findType(name, TAG_COMPOUND);
    }
    
    public byte findByte(String name, byte def)
    {
        TagByte t = (TagByte)findType(name, TAG_BYTE);
        
        if (t == null)
        {
            return def;
        }
        
        return t.value;
    }
    
    public short findShort(String name, short def)
    {
        TagShort t = (TagShort)findType(name, TAG_SHORT);
        
        if (t == null)
        {
            return def;
        }
        
        return t.value;
    }
    
    public int findInt(String name, int def)
    {
        TagInt t = (TagInt)findType(name, TAG_INT);
        
        if (t == null)
        {
            return def;
        }
        
        return t.value;
    }
    
    public long findLong(String name, long def)
    {
        TagLong t = (TagLong)findType(name, TAG_LONG);
        
        if (t == null)
        {
            return def;
        }
        
        return t.value;
    }
    
    public float findFloat(String name, float def)
    {
        TagFloat t = (TagFloat)findType(name, TAG_FLOAT);
        
        if (t == null)
        {
            return def;
        }
        
        return t.value;
    }
    
    public double findDouble(String name, byte def)
    {
        TagDouble t = (TagDouble)findType(name, TAG_DOUBLE);
        
        if (t == null)
        {
            return def;
        }
        
        return t.value;
    }
    
    public IntegerV findIntV(String name, int def)
    {
        return findIntV(name, new IntegerV(def));
    }
    public IntegerV findIntV(String name, IntegerV def)
    {
        TagIntV t = (TagIntV)findType(name, TAG_INTV);
        
        if (t == null)
        {
            return def;
        }
        
        return t.value;
    }
    
    public int findIntVAsInt(String name, int def)
    {
        return findIntV(name, def).value;
    }
    public int findIntVAsInt(String name, IntegerV def)
    {
        return findIntV(name, def).value;
    }
    
    public String findString(String name)
    {
        return findString(name, null);
    }
    public String findString(String name, String def)
    {
        TagString t = (TagString)findType(name, TAG_STRING);
        
        if (t == null)
        {
            return def;
        }
        
        return t.value;
    }
    
    public TagData findDataTag(String name)
    {
        return (TagData)findType(name, TAG_DATA);
    }
    
    public TagDoubleVec2 findDoubleVec2Tag(String name)
    {
        return (TagDoubleVec2)findType(name, TAG_DOUBLE_VEC2);
    }
    
    public TagDoubleVec3 findDoubleVec3Tag(String name)
    {
        return (TagDoubleVec3)findType(name, TAG_DOUBLE_VEC3);
    }
    
    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        appendString(str, 0);
        
        return str.toString();
    }
    
    private void appendString(StringBuilder str, int depth)
    {
        String indentation = Util.stringRepeat("    ", depth);
        
        str
                .append(indentation)
                .append("[TagCompound name=\"")
                .append(name).append("\"]\n")
                .append(indentation)
                .append("{");
        
        for (int i = 0; i < tags.size(); i++)
        {
            Tag tag = tags.get(i);
            str.append("\n");
            
            if (tag instanceof TagCompound)
            {
                ((TagCompound)tag).appendString(str, depth + 1);
            }
            else
            {
                str.append(indentation).append("    ").append(tag.toString());
            }
        }
        
        str.append("\n").append(indentation).append("}");
    }
}
