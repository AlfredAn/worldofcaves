package worldofcaves;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import org.lwjgl.util.vector.Vector3f;

public final class Util
{
    public static final Random random = new Random();
    
    public static int min(int x, int y)
    {
        if (x < y)
        {
            return x;
        }
        return y;
    }
    
    public static int max(int x, int y)
    {
        if (x > y)
        {
            return x;
        }
        return y;
    }
    
    public static int abs(int x)
    {
        if (x > 0)
        {
            return x;
        }
        return -x;
    }
    
    public static float abs(float x)
    {
        if (x > 0)
        {
            return x;
        }
        return -x;
    }
    
    public static double abs(double x)
    {
        if (x > 0)
        {
            return x;
        }
        return -x;
    }
    
    public static double cotan(double x)
    {
        return 1. / Math.tan(x);
    }
    
    public static float clamp(float x, float min, float max)
    {
        return Math.min(Math.max(x, min), max);
    }
    
    public static double clamp(double x, double min, double max)
    {
        return Math.min(Math.max(x, min), max);
    }
    
    public static int mod(int x, int mod)
    {
        return (x % mod + mod) % mod;
    }
    
    public static double modD(double x, double mod)
    {
        return (x % mod + mod) % mod;
    }
    
    public static int sqr(int x)
    {
        return x * x;
    }
    
    public static float sqr(float x)
    {
        return x * x;
    }
    
    public static double sqr(double x)
    {
        return x * x;
    }
    
    public static float mix(float a, float b, float f)
    {
        return a * (1 - f) + b * f;
    }
    
    public static double mix(double a, double b, double f)
    {
        return a * (1 - f) + b * f;
    }
    
    public static Vector3f rotateVector(Vector3f vec, double rotx, double roty, double rotz)
    {
        Vector3f result = new Vector3f(vec);
        Vector3f vec2 = new Vector3f(vec);
        
        if (rotx != 0) 
        {
            result.y = (float)(vec2.y*Math.cos(rotx) - vec2.z*Math.sin(rotx));
            result.z = (float)(vec2.y*Math.sin(rotx) + vec2.z*Math.cos(rotx));
            vec2.y = result.y;
            vec2.z = result.z;
        }
        
        if (roty != 0) 
        {
            result.x = (float)(vec.x*Math.cos(roty) + vec.z*Math.sin(roty));
            result.z = (float)(-vec.x*Math.sin(roty) + vec.z*Math.cos(roty));
            vec2.x = result.x;
            vec2.z = result.z;
        }
        
        if (rotz != 0) 
        {
            result.x = (float)(vec2.x*Math.cos(rotz) - vec2.y*Math.sin(rotz));
            result.y = (float)(vec2.x*Math.sin(rotz) + vec2.y*Math.cos(rotz));
        }
        
        return result;
    }
    
    public static float pointDistanceSqr(Vector3f p1, Vector3f p2)
    {
        return Vector3f.sub(p1, p2, null).lengthSquared();
    }
    
    public static float pointDistance(Vector3f p1, Vector3f p2)
    {
        return Vector3f.sub(p1, p2, null).length();
    }
    
    public static float pointDistance(float x1, float y1, float x2, float y2)
    {
        return (float)Math.sqrt(pointDistanceSqr(x1, y1, x2, y2));
    }
    public static float pointDistanceSqr(float x1, float y1, float x2, float y2)
    {
        return sqr(x1 - x2) + sqr(y1 - y2);
    }
    
    public static float originDistance(float x, float y)
    {
        return (float)Math.sqrt(x * x + y * y);
    }
    public static float originDistanceSqr(float x, float y)
    {
        return x * x + y * y;
    }
    
    public static float originDistance(float x, float y, float z)
    {
        return (float)Math.sqrt(x * x + y * y + z * z);
    }
    public static float originDistanceSqr(float x, float y, float z)
    {
        return x * x + y * y + z * z;
    }
    
    public static double originDistance(double x, double y, double z)
    {
        return Math.sqrt(x * x + y * y + z * z);
    }
    public static double originDistanceSqr(double x, double y, double z)
    {
        return x * x + y * y + z * z;
    }
    
    public static String coordString(int x, int y, int z)
    {
        return "(" + Integer.toString(x) + ", " + Integer.toString(y) + ", " + Integer.toString(z) + ")";
    }
    
    public static String coordString(double x, double y, double z)
    {
        return "(" + Double.toString(x) + ", " + Double.toString(y) + ", " + Double.toString(z) + ")";
    }
    
    private Util() {}
    
    private static final Class cl = new Util().getClass();
    
    public static String getAbsolutePath(String path)
    {
        return cl.getResource("/" + path).getPath();
    }
    
    public static InputStream getResourceAsStream(String path)
    {
        return cl.getResourceAsStream("/" + path);
    }
    
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    public static String getCurrentTimeStamp()
    {
        Date now = new Date();
        return sdf.format(now);
    }
    
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes)
    {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++)
        {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    private static final byte[] bits = new byte[]
    {
        0x1, 0x2, 0x4, 0x8,
        (byte)0x10, (byte)0x20, (byte)0x40, (byte)0x80
    };
    public static String bytesToBin(byte[] bytes)
    {
        char[] chars = new char[bytes.length * 8];
        for (int i = 0; i < bytes.length; i++)
        {
            byte b = bytes[i];
            
            for (int j = 0; j < 8; j++)
            {
                chars[i * 8 + 7 - j] = ((b & bits[j]) == 0) ? '0' : '1';
            }
        }
        return new String(chars);
    }
    
    public static String stringRepeat(String str, int n)
    {
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < n; i++)
        {
            sb.append(str);
        }
        
        return sb.toString();
    }
}