package worldofcaves.io;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import worldofcaves.Util;

public abstract class Tag implements Enumerable
{
    public static final byte
            TAG_END = 0,
            TAG_COMPOUND = 1,
            TAG_BYTE = 2,
            TAG_SHORT = 3,
            TAG_INT = 4,
            TAG_LONG = 5,
            TAG_FLOAT = 6,
            TAG_DOUBLE = 7,
            TAG_INTV = 8,
            TAG_STRING = 9,
            TAG_DATA = 10,
            TAG_DOUBLE_VEC3 = 11,
            TAG_DOUBLE_VEC2 = 12;
    
    protected static final Charset stringFormat = StandardCharsets.UTF_8;
    protected static final ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
    
    public final String name;
    
    public Tag(String name)
    {
        this.name = name;
        
        if (stringSize(name) > IntegerV.MAX_VALUE)
        {
            throw new IllegalArgumentException("Name is too long.");
        }
    }
    
    /*
    Data format: (all values are big endian except intv)
    
    byte tagId;
    intv nameLength;
    byte[] name;
    --data--
    */
    
    public final void bufferData(ByteBuffer buf)
    {
        IntegerV.putBuffer(getId(), buf);
        putString(name, buf);
        getData(buf);
    }
    
    protected abstract void getData(ByteBuffer buf);
    
    public final int getHeaderSize()
    {
        return 1 + stringSize(name);
    }
    
    public final int getSize()
    {
        return getHeaderSize() + getDataSize();
    }
    
    protected abstract int getDataSize();
    
    private static String readString(DataInputStream in) throws IOException
    {
        int length = IntegerV.readAsInt(in);
        byte[] bytes = new byte[length];
        in.readFully(bytes, 0, length);
        
        return new String(bytes, stringFormat);
    }
    
    protected static void putString(String str, ByteBuffer buf)
    {
        byte[] bytes = encodeString(str);
        
        IntegerV.putBuffer(bytes.length, buf);
        buf.put(bytes);
    }
    
    protected static int stringSize(String str)
    {
        int size = encodeString(str).length;
        return size + IntegerV.getSize(size);
    }
    
    private static byte[] encodeString(String str)
    {
        return str.getBytes(stringFormat);
    }
    
    public ByteBuffer toByteBuffer()
    {
        int size = getSize();
        
        ByteBuffer buf = ByteBuffer.allocateDirect(size).order(byteOrder);
        bufferData(buf);
        buf.flip();
        
        return buf;
    }
    
    public static Tag read(InputStream in) throws IOException
    {
        if (in instanceof DataInputStream)
        {
            return read((DataInputStream)in);
        }
        
        return read(new DataInputStream(in));
    }
    private static Tag read(DataInputStream in) throws IOException
    {
        byte id = in.readByte();
        
        if (id == TAG_END)
        {
            return null;
        }
        
        String name = readString(in);
        
        switch (id)
        {
            case TAG_COMPOUND:
                TagCompound tc = new TagCompound(name);
                while (true)
                {
                    Tag t = Tag.read(in);
                    
                    if (t == null)
                    {
                        break;
                    }
                    else
                    {
                        tc.add(t);
                    }
                }
                return tc;
            case TAG_BYTE:
                return new TagByte(name, in.readByte());
            case TAG_SHORT:
                return new TagShort(name, in.readShort());
            case TAG_INT:
                return new TagInt(name, in.readInt());
            case TAG_LONG:
                return new TagLong(name, in.readLong());
            case TAG_FLOAT:
                return new TagFloat(name, in.readFloat());
            case TAG_DOUBLE:
                return new TagDouble(name, in.readDouble());
            case TAG_INTV:
                return new TagIntV(name, IntegerV.read(in));
            case TAG_STRING:
                return new TagString(name, readString(in));
            case TAG_DATA:
                int size = IntegerV.readAsInt(in);
                byte[] bytes = new byte[size];
                in.readFully(bytes, 0, size);
                return new TagData(name, bytes);
            case TAG_DOUBLE_VEC3:
                return new TagDoubleVec3(name, in.readDouble(), in.readDouble(), in.readDouble());
            case TAG_DOUBLE_VEC2:
                return new TagDoubleVec2(name, in.readDouble(), in.readDouble());
            default:
                throw new TagFormatException("Unknown tag id: " + id);
        }
    }
    
    public void write(OutputStream os) throws IOException
    {
        ByteBuffer buf = toByteBuffer();
        WritableByteChannel wbc = Channels.newChannel(os);
        wbc.write(buf);
    }
    
    public String getName()
    {
        return getName(getId());
    }
    
    public static String getName(short tagId)
    {
        switch (tagId)
        {
            case TAG_END:
                return "TagEnd";
            case TAG_COMPOUND:
                return "TagCompound";
            case TAG_BYTE:
                return "TagByte";
            case TAG_SHORT:
                return "TagShort";
            case TAG_INT:
                return "TagInt";
            case TAG_LONG:
                return "TagLong";
            case TAG_FLOAT:
                return "TagFloat";
            case TAG_DOUBLE:
                return "TagDouble";
            case TAG_INTV:
                return "TagIntV";
            case TAG_STRING:
                return "TagString";
            case TAG_DATA:
                return "TagData";
            case TAG_DOUBLE_VEC3:
                return "TagDoubleVec3";
            case TAG_DOUBLE_VEC2:
                return "TagDoubleVec2";
            default:
                return "Unknown";
        }
    }
    
    @Override
    public String toString()
    {
        ByteBuffer buf = toByteBuffer();
        
        int hs = getHeaderSize();
        int len = buf.limit() - hs;
        
        byte[] bytes = new byte[len];
        
        buf.position(hs);
        buf.get(bytes);
        
        return "[" + getName() + " name=\"" + name + "\", data=" + Util.bytesToHex(bytes) + "]";
    }
}
