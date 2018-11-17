package worldofcaves;

import java.nio.ByteBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL30.*;
import worldofcaves.model.BasicModels;

public class Framebuffer
{
    private int fboId, fboTex, fboDepth, width, height, msaa, texTarget;
    
    public Framebuffer(int width, int height)
    {
        this(width, height, 1);
    }
    private Framebuffer(int width, int height, int msaa)
    {
        WorldOfCaves.log("Creating framebuffer...");
        
        this.width = width;
        this.height = height;
        this.msaa = msaa;
        
        if (msaa <= 1)
        {
            create();
        }
        else
        {
            throw new UnsupportedOperationException("MSAA is not supported.");
            //createMultisample();
        }
        
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        
        WorldOfCaves.checkGLErrors("Framebuffer()");
    }
    
    private void create()
    {
        texTarget = GL_TEXTURE_2D;
        
        fboDepth = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, fboDepth);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, width, height);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);
        
        fboTex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, fboTex);
        
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB10_A2, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer)null);
        
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);  
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);  
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 1);
        glBindTexture(GL_TEXTURE_2D, 0);
        
        fboId = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fboId);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, fboTex, 0);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, fboDepth);
        
        WorldOfCaves.checkGLErrors("Framebuffer.create()");
        
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE)
        {
            WorldOfCaves.forceExit("Couldn't create framebuffer: Error " + status);
        }
        
        WorldOfCaves.checkGLErrors("Framebuffer.create()");
    }
    
    /*@Deprecated
    private void createMultisample()
    {
        texTarget = GL_TEXTURE_2D_MULTISAMPLE;
        
        fboDepth = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, fboDepth);
        glRenderbufferStorageMultisample(GL_RENDERBUFFER, msaa, GL_DEPTH_COMPONENT, width, height);
        glBindRenderbuffer(GL_RENDERBUFFER, 0);
        
        fboTex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, fboTex);
        
        glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, msaa, GL_RGBA, width, height, true);
        
        glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, 0);
        
        fboId = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fboId);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D_MULTISAMPLE, fboTex, 0);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, fboDepth);
        
        WorldOfCaves.checkGLErrors("Framebuffer.createMultisample()");
        
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE)
        {
            WorldOfCaves.forceExit("Couldn't create framebuffer: Error " + status);
        }
        
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        
        WorldOfCaves.checkGLErrors("Framebuffer.createMultisample()");
    }*/
    
    public void bind(int target)
    {
        glBindFramebuffer(target, fboId);
    }
    
    public void unbind(int target)
    {
        glBindFramebuffer(target, 0);
    }
    
    public int getTexture()
    {
        return fboTex;
    }
    
    public void bindTexture()
    {
        glBindTexture(texTarget, fboTex);
    }
    
    public void unbindTexture()
    {
        glBindTexture(texTarget, 0);
    }
    
    public void genMipmap()
    {
        glGenerateMipmap(texTarget);
    }
    
    public void draw()
    {
        draw(1, 1);
    }
    public void draw(boolean useQuad)
    {
        draw(1, 1, useQuad);
    }
    public void draw(double xScale, double yScale)
    {
        draw(xScale, yScale, false);
    }
    public void draw(double xScale, double yScale, boolean useQuad)
    {
        if (useQuad)
        {
            bindTexture();
            ModelLoader.basicModels.draw(null, BasicModels.QUAD);
            unbindTexture();
        }
        else
        {
            glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
            bind(GL_READ_FRAMEBUFFER);

            glDrawBuffer(GL_BACK);
            glBlitFramebuffer(0, 0, width, height, 0, 0, (int)(width * xScale), (int)(height * yScale), GL_COLOR_BUFFER_BIT, GL_LINEAR);

            glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
        }
        
        WorldOfCaves.checkGLErrors("Framebuffer.draw()");
    }
    
    public void destroy()
    {
        glBindTexture(texTarget, 0);
        glDeleteTextures(fboTex);
        
        glBindRenderbuffer(GL_RENDERBUFFER, 0);
        glDeleteRenderbuffers(fboDepth);
        
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glDeleteFramebuffers(fboId);
        
        WorldOfCaves.checkGLErrors("Framebuffer.destroy()");
    }
}