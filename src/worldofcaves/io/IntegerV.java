package worldofcaves.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import worldofcaves.Util;

/**
 * <p><b><i>IntegerV format:</i></b>
 * <br />
 * <br />The payload is little-endian.
 * <br />
 * <br /><b>First byte:</b>
 * <br /><i>bits 0-6:</i>    6 bits of payload
 * <br />  <i>bit  7:</i>    sign bit
 * <br />  <i>bit  8:</i>    if 0, this is the last byte
 * <br />
 * <br /><b>Second and third bytes:</b>
 * <br /><i>bits 0-7:</i>    7 bits of payload
 * <br />  <i>bit  8:</i>    if 0, this is the last byte
 * <br />
 * <br /><b>Fourth byte:</b>
 * <br /><i>bits 0-8:</i>    8 bits of payload</p>
 * 
 * @author Alfred Andersson
 */
public class IntegerV extends Number implements Comparable<IntegerV>
{
    public static final int
            MAX_1B_VALUE = 63,
            MIN_1B_VALUE = -64,
            
            MAX_2B_VALUE = 8191,
            MIN_2B_VALUE = -8192,
            
            MAX_3B_VALUE = 1048575,
            MIN_3B_VALUE = -1048576,
            
            MAX_VALUE = 268435455,
            MIN_VALUE = -268435456,
            
            MAX_SIZE = 4,
            MIN_SIZE = 1;
    
    public final int value;
    private byte[] data;
    
    public IntegerV(byte[] data)
    {
        if (data.length == 0)
        {
            this.data = new byte[0];
            value = 0;
        }
        else
        {
            int len = Util.min(data.length, 4);
            
            this.data = new byte[len];
            System.arraycopy(data, 0, this.data, 0, len);
            
            value = decode(this.data);
        }
    }
    public IntegerV(int value)
    {
        if (value > MAX_VALUE || value < MIN_VALUE)
        {
            throw new IllegalArgumentException("Value out of bounds: " + value);
        }
        
        this.value = value;
    }
    
    public byte getSize()
    {
        if (data != null)
        {
            return (byte)data.length;
        }
        
        return getSize(value);
    }
    
    public byte[] encode()
    {
        if (data == null)
        {
            data = encode(value);
        }
        
        return data;
    }
    
    public static byte getSize(int value)
    {
        if (value <= MAX_1B_VALUE && value >= MIN_1B_VALUE)
        {
            return 1;
        }
        else if (value <= MAX_2B_VALUE && value >= MIN_2B_VALUE)
        {
            return 2;
        }
        else if (value <= MAX_3B_VALUE && value >= MIN_3B_VALUE)
        {
            return 3;
        }
        else if (value <= MAX_VALUE && value >= MIN_VALUE)
        {
            return 4;
        }
        
        throw new IllegalArgumentException("Value out of bounds: " + value);
    }
    
    public static byte[] encode(int value)
    {
        //1:  6-bit + sign
        //2: 13-bit + sign
        //3: 20-bit + sign
        //4: 28-bit + sign
        
        byte size = getSize(value);
        
        byte[] res = new byte[size];
        byte sign = (byte)(((value >>> 25) & 0x40) >>> 6);
        //System.out.println(sign);
        
                         //followup                sign bit                 bits 0-6
        res[0] = (byte)((size > 1 ? 0x80 : 0) + ((value >>> 25) & 0x40) + (value & 0x3F));
        
        if (size >= 2)
        {                                              //bits 7-13
            res[1] = (byte)((size > 2 ? 0x8000 : 0) + ((value & 0x1FC0) << 2) >>> 8);
            
            if (size >= 3)
            {                                                 //bits 14-20
                res[2] = (byte)((size > 3 ? 0x800000 : 0) + ((value & 0xFE000) << 3) >>> 16);
                
                if (size == 4)
                {                     //bits 21-28
                    res[3] = (byte)(((value & 0xFF00000) << 4) >>> 24);
                }
            }
        }
        
        return res;
    }
    
    public static int decode(byte[] data)
    {
        byte size = (byte)data.length;
        
        if (size == 0)
        {
            return 0;
        }
        
        byte actualSize;
        int res = 0;
        
        byte d0 = data[0];
        res += d0 & 0x3F;
        if ((d0 & 0x80) != 0)
        {
            if (size < 2)
            {
                throw new IllegalArgumentException("Invalid number format.");
            }
            
            byte d = data[1];
            res += (d & 0x7F) << 6;
            if ((d & 0x80) != 0)
            {
                if (size < 3)
                {
                    throw new IllegalArgumentException("Invalid number format.");
                }
                
                d = data[2];
                res += (d & 0x7F) << 13;
                if ((d & 0x80) != 0)
                {
                    if (size < 4)
                    {
                        throw new IllegalArgumentException("Invalid number format.");
                    }
                    
                    d = data[3];
                    res += ((int)d + 128 ^ 0x80) << 20;
                    
                    actualSize = 4;
                }
                else
                {
                    actualSize = 3;
                }
            }
            else
            {
                actualSize = 2;
            }
        }
        else
        {
            actualSize = 1;
        }
        
        if ((d0 & 0x40) != 0)
        {
            switch (actualSize)
            {
                case 1:
                    res += MIN_1B_VALUE;
                    break;
                case 2:
                    res += MIN_2B_VALUE;
                    break;
                case 3:
                    res += MIN_3B_VALUE;
                    break;
                case 4:
                    res += MIN_VALUE;
                    break;
            }
        }
        
        return res;
    }
    
    public void putBuffer(ByteBuffer buf)
    {
        buf.put(encode());
    }
    
    public static void putBuffer(int value, ByteBuffer buf)
    {
        buf.put(encode(value));
    }
    
    private static final byte[] bytes = new byte[4];
    
    public static IntegerV read(DataInputStream in) throws IOException
    {
        return new IntegerV(readAsBytes(in));
    }
    public static int readAsInt(DataInputStream in) throws IOException
    {
        return decode(readAsBytes(in));
    }
    public static byte[] readAsBytes(DataInputStream in) throws IOException
    {
        byte size;
        
        byte d = in.readByte();
        bytes[0] = d;
        if ((d & 0x80) != 0)
        {
            d = in.readByte();
            bytes[1] = d;
            if ((d & 0x80) != 0)
            {
                d = in.readByte();
                bytes[2] = d;
                if ((d & 0x80) != 0)
                {
                    bytes[3] = in.readByte();
                    size = 4;
                }
                else
                {
                    size = 3;
                }
            }
            else
            {
                size = 2;
            }
        }
        else
        {
            size = 1;
        }
        
        byte[] res = new byte[size];
        System.arraycopy(bytes, 0, res, 0, size);
        
        return res;
    }
    
    @Override
    public String toString()
    {
        return Integer.toString(value);
    }
    
    @Override
    public boolean equals(Object o)
    {
        return (o instanceof IntegerV && ((IntegerV)o).value == value);
    }
    
    @Override
    public int hashCode()
    {
        return value;
    }
    
    @Override
    public int compareTo(IntegerV iv)
    {
        return (value < iv.value) ? -1 : ((value == iv.value) ? 0 : 1);
    }
    
    @Override
    public byte byteValue()
    {
        return (byte)value;
    }
    
    @Override
    public short shortValue()
    {
        return (short)value;
    }
    
    @Override
    public int intValue()
    {
        return value;
    }
    
    @Override
    public long longValue()
    {
        return (long)value;
    }
    
    @Override
    public float floatValue()
    {
        return (float)value;
    }
    
    @Override
    public double doubleValue()
    {
        return (double)value;
    }
}
