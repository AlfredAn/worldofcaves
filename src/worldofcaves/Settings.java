package worldofcaves;

import org.lwjgl.opengl.ContextCapabilities;
import static org.lwjgl.opengl.EXTTextureFilterAnisotropic.*;
import static org.lwjgl.opengl.GL11.glGetInteger;
import org.lwjgl.opengl.GLContext;
import worldofcaves.game.World;

public final class Settings
{
    private static double ssaa, fov, zNear, zFar, renderDist, lightmapDist;
    private static int filter, maxAf, lightmapXSize, lightmapYSize, shadowMode;
    
    public static final int SHADOWS_NONE = 0, SHADOWS_VERTEX = 1, SHADOWS_FRAGMENT = 2;
    
    private Settings() {}
    
    public static final void init()
    {
        WorldOfCaves.log("Initializing settings...");
        
        ssaa = 4;
        fov = 60;
        zNear = 1./12;
        zFar = World.renderDistBase * 2;
        lightmapXSize = 4096*2;
        lightmapYSize = 4096*2;
        lightmapDist = World.renderDistBase;
        shadowMode = SHADOWS_FRAGMENT;
        
        ContextCapabilities cc = GLContext.getCapabilities();
        
        if (cc.GL_EXT_texture_filter_anisotropic)
        {
            maxAf = glGetInteger(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
            filter = Util.min(16, maxAf);
        }
        else
        {
            filter = -4;
        }
        
        WorldOfCaves.checkGLErrors("Settings.init()");
    }
    
    public static double getSSAA()
    {
        return ssaa;
    }
    
    public static int getMaxAF()
    {
        return maxAf;
    }
    
    /**
     *
     * @return The current active filter.
     * 
     * <ul><li>equal to zero: no filtering</li>
     * <li>less than zero: mipmapping</li>
     * <li>greater than zero: anisotropic filtering</li></ul>
     */
    public static int getFilter()
    {
        return filter;
    }
    
    public static double getFov()
    {
        return fov;
    }
    
    public static double getZNear()
    {
        return zNear;
    }
    
    public static double getZFar()
    {
        return zFar;
    }
    
    /*public static double getRenderDist()
    {
        return renderDist;
    }*/
    
    public static int getLightmapXSize()
    {
        return lightmapXSize;
    }
    
    public static int getLightmapYSize()
    {
        return lightmapYSize;
    }
    
    public static double getLightmapDist()
    {
        return lightmapDist;
    }
    
    public static int getShadowMode()
    {
        return shadowMode;
    }
}
