package worldofcaves;

import java.nio.ByteBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL30.*;

public class Lightmap
{
    private int fboId, fboDepth, width, height;
    
    public Lightmap(int width, int height)
    {
        WorldOfCaves.log("Creating lightmap...");
        
        this.width = width;
        this.height = height;
        
        create();
        
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        
        WorldOfCaves.checkGLErrors("Lightmap()");
    }
    
    private void create()
    {
        fboId = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fboId);
        
        fboDepth = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, fboDepth);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32, width, height, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer)null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, fboDepth, 0);
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);
        
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE)
        {
            WorldOfCaves.forceExit("Couldn't create lightmap framebuffer: Error " + status);
        }
        
        WorldOfCaves.checkGLErrors("Lightmap.create()");
    }
    
    public void bind(int target)
    {
        glBindFramebuffer(target, fboId);
    }
    
    public void unbind(int target)
    {
        glBindFramebuffer(target, 0);
    }
    
    public void bindTexture()
    {
        glBindTexture(GL_TEXTURE_2D, fboDepth);
    }
    
    public void unbindTexture()
    {
        glBindTexture(GL_TEXTURE_2D, 0);
    }
    
    /*public void draw()
    {
        draw(1, 1);
    }
    public void draw(double xScale, double yScale)
    {
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
        bind(GL_READ_FRAMEBUFFER);
        
        glDrawBuffer(GL_BACK);
        glBlitFramebuffer(0, 0, width, height, 0, 0, (int)(width * xScale), (int)(height * yScale), GL_COLOR_BUFFER_BIT, GL_LINEAR);
        
        glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
        
        WorldOfCaves.checkGLErrors("Framebuffer.draw()");
    }*/
    
    public void destroy()
    {
        glBindTexture(GL_TEXTURE_2D, 0);
        glDeleteTextures(fboDepth);
        
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glDeleteFramebuffers(fboId);
        
        WorldOfCaves.checkGLErrors("Lightmap.destroy()");
    }
}