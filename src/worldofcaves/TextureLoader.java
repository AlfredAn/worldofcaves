package worldofcaves;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;

public class TextureLoader
{
    public static int block;
    
    public static void load()
    {
        WorldOfCaves.log("Loading textures...");
        
        block = loadPNGTexture("textures/game/block.png", GL_TEXTURE0, Settings.getFilter());
        
        WorldOfCaves.checkGLErrors("TextureLoader.load()");
    }
    
    public static void unload()
    {
        WorldOfCaves.log("Unloading textures...");
        
        glDeleteTextures(block);
        
        WorldOfCaves.checkGLErrors("TextureLoader.unload()");
    }
    
    /*
    filter:
        == 0: no filtering
         < 0: mipmapping
         > 0: anisotropic filtering
    */
    private static int loadPNGTexture(String filename, int textureUnit, int filter)
    {
        ByteBuffer ibuf = null;
        int imgWidth = 0;
        int imgHeight = 0;
        int imgSize = 0;
        InputStream in = null;
        String path = Util.getAbsolutePath(filename);
        
        try
        {
            in = Util.getResourceAsStream(filename);
            PNGDecoder decoder = new PNGDecoder(in);
            
            imgWidth = decoder.getWidth();
            imgHeight = decoder.getHeight();
            imgSize = 4 * imgWidth * imgHeight;
            
            ibuf = BufferUtils.createByteBuffer(imgSize);
            decoder.decode(ibuf, imgWidth * 4, Format.RGBA);
            ibuf.flip();
        }
        catch (IOException e)
        {
            WorldOfCaves.forceExit("Error loading texture: " + filename, e);
        }
        finally
        {
            try
            {
                if (in != null)
                {
                    in.close();
                }
            }
            catch (IOException e)
            {
                WorldOfCaves.forceExit("Error loading texture: " + filename, e);
            }
        }
        
        int texWidth = 16;
        int texHeight = 16;
        int texDepth = 256;
        int texSize = 4 * texWidth * texHeight * texDepth;
        
        ByteBuffer buf = BufferUtils.createByteBuffer(texSize);
        
        for (int z = 0; z < texDepth; z++)
        {
            int bx = (z % 16) * 16;
            int by = (z / 16) * 16;
            
            for (int y = 0; y < texHeight; y++)
            {
                int iy = by + y;
                
                for (int x = 0; x < texWidth; x++)
                {
                    int ix = bx + x;
                    int i = (ix + iy * 256) * 4;
                    
                    buf.put(ibuf.get(i+0));
                    buf.put(ibuf.get(i+1));
                    buf.put(ibuf.get(i+2));
                    buf.put(ibuf.get(i+3));
                }
            }
        }
        
        /*int texWidth = imgWidth * 4;
        int texHeight = imgHeight * 4;
        int texSize = 4 * texWidth * texHeight;
        int xs = 16, ys = 16, xts = xs * 3, yts = ys * 3;
        
        ByteBuffer buf = BufferUtils.createByteBuffer(texSize);
        
        for (int y = 0; y < texHeight; y++)
        {
            int ny = y / yts;
            int my = y % ys;
            int ay = ny * ys + my;
            
            for (int x = 0; x < texWidth; x++)
            {
                if (x >= imgWidth * 3 || y >= imgHeight * 3)
                {
                    buf.put((byte)0);
                    buf.put((byte)0);
                    buf.put((byte)0);
                    buf.put((byte)0);
                    continue;
                }
                
                int nx = x / xts;
                int mx = x % xs;
                int ax = nx * xs + mx;
                int i = ax * 4 + ay * 4 * imgWidth;
                
                buf.put(ibuf.get(i+0));
                buf.put(ibuf.get(i+1));
                buf.put(ibuf.get(i+2));
                buf.put(ibuf.get(i+3));
            }
        }*/
        
        buf.flip();
        
        int texId = glGenTextures();
        glActiveTexture(textureUnit);
        glBindTexture(GL_TEXTURE_2D_ARRAY, texId);
        
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        
        glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, GL_RGBA, texWidth, texHeight, texDepth, 0,
                GL_RGBA, GL_UNSIGNED_BYTE, buf);
        
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_REPEAT);
        
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER,
                GL_NEAREST);
        
        int minFilter;
        
        if (filter != 0)
        {
            minFilter = GL_LINEAR_MIPMAP_LINEAR;
        }
        else
        {
            minFilter = GL_NEAREST;
        }
        
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER,
                minFilter); //GL_NEAREST_MIPMAP_LINEAR
        
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 4);
        
        if (filter > 0)
        {
            glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAX_ANISOTROPY_EXT, filter);
            
            glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_BASE_LEVEL, 0);
            glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAX_LEVEL, 4);
            glGenerateMipmap(GL_TEXTURE_2D_ARRAY);
        }
        else if (filter < 0)
        {
            glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_BASE_LEVEL, 0);
            glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAX_LEVEL, Math.min(-filter, 4));
            glGenerateMipmap(GL_TEXTURE_2D_ARRAY);
        }
        
        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
        
        WorldOfCaves.checkGLErrors("TextureLoader.loadTexture(\"" + path + "\")");
        
        return texId;
    }
}
