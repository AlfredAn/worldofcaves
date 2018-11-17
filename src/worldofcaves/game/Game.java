package worldofcaves.game;

import java.io.IOException;
import org.lwjgl.opengl.Display;
import worldofcaves.MatrixHandler;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import worldofcaves.Framebuffer;
import worldofcaves.Lightmap;
import worldofcaves.Settings;
import worldofcaves.Shader;
import worldofcaves.Util;
import worldofcaves.WorldOfCaves;
import worldofcaves.game.generator.HillsGenerator;
import worldofcaves.io.WorldIO;

public final class Game
{
    private final World world;
    private int prevWidth, prevHeight;
    protected Updater updater;
    private boolean isDestroyed = false;
    
    private static double xSamples = Math.sqrt(Settings.getSSAA()), ySamples = xSamples;
    
    private Framebuffer fb;
    private Lightmap sunlight;
    
    public MatrixHandler mat, matGUI, matSunlight;
    
    public Game()
    {
        WorldOfCaves.log("Starting game...");
        
        mat = new MatrixHandler();
        matGUI = new MatrixHandler();
        matSunlight = new MatrixHandler();
        
        DisplayMode dm = Display.getDisplayMode();
        prevWidth = dm.getWidth();
        prevHeight = dm.getHeight();
        
        mat.setProjection(Settings.getFov(), (double)prevWidth / prevHeight, Settings.getZNear(), Settings.getZFar());
        mat.updateFrustum();
        
        double s = Settings.getLightmapDist();
        matSunlight.setProjectionOrtho(-s, -s, -s, s, s, s);
        matSunlight.setView(0, 0, 0, 0, 45, 0);
        matSunlight.updateFrustum();
        
        Shader.block.use();
        glUniform1i(Shader.block.getUniformLocation(1), 0);
        glUniform1i(Shader.block.getUniformLocation(4), 1);
        glUniform1f(Shader.block.getUniformLocation(5), Settings.getLightmapXSize());
        glUniform1f(Shader.block.getUniformLocation(6), Settings.getLightmapYSize());
        glUniform1i(Shader.block.getUniformLocation(7), Settings.getShadowMode());
        glUseProgram(0);
        
        glEnable(GL_CULL_FACE);
        glFrontFace(GL_CCW);
        glCullFace(GL_BACK);
        
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        fb = new Framebuffer((int)(prevWidth * xSamples), (int)(prevHeight * ySamples));
        
        if (Settings.getShadowMode() != 0)
        {
            sunlight = new Lightmap(Settings.getLightmapXSize(), Settings.getLightmapYSize());
        }
        
        updater = new Updater(this);
        //world = new World(this, "World");
        World w = null;
        try
        {
            w = WorldIO.loadWorld(this, "World");
            WorldOfCaves.log("World loaded.");
        }
        catch (IOException e)
        {
            WorldOfCaves.logError("Unable to load world.", e);
            w = new World(this, "World", new HillsGenerator());
        }
        
        world = w;//new World(this, "World", new HillsGenerator());//w;
        
        WorldOfCaves.checkGLErrors("Game()");
    }
    
    public void update()
    {
        DisplayMode dm = Display.getDisplayMode();
        if (dm.getWidth() != prevWidth || dm.getHeight() != prevHeight)
        {
            prevWidth = dm.getWidth();
            prevHeight = dm.getHeight();
            mat.setProjection(Settings.getFov(), (double)prevWidth / prevHeight, Settings.getZNear(), Settings.getZFar());
            
            fb.destroy();
            fb = new Framebuffer((int)(prevWidth * xSamples), (int)(prevHeight * ySamples));
        }
        
        world.update();
        updater.finish();
    }
    
    public static final int DRAW_ACTUAL = 0, DRAW_LIGHTMAP = 1;
    private final float[] skyColor = new float[3];
    
    public void draw()
    {
        if (Settings.getShadowMode() != 0)
        {
            sunlight.bind(GL_FRAMEBUFFER);
            glViewport(0, 0, Settings.getLightmapXSize(), Settings.getLightmapYSize());

            glClear(GL_DEPTH_BUFFER_BIT);

            glDisable(GL_CULL_FACE);
            double[] pos = world.getPlayer().getPos();
            matSunlight.setView((int)(pos[0]/16)*16, (int)(pos[1]/16)*16, (int)(pos[2]/16)*16, 0, (float)(world.getSunAngle() - Math.PI/2), 0);
            matSunlight.updateFrustum();
            world.draw(DRAW_LIGHTMAP, matSunlight, null);

            sunlight.unbind(GL_FRAMEBUFFER);
        }
        
        fb.bind(GL_FRAMEBUFFER);
        
        DisplayMode dm = Display.getDisplayMode();
        int width = dm.getWidth();
        int height = dm.getHeight();
        glViewport(0, 0, (int)(width * xSamples), (int)(height * ySamples));
        
        world.getSkyColor(skyColor);
        glClearColor(skyColor[0], skyColor[1], skyColor[2], 1f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        //Shader.block.use();
        //matSunlight.uniformMVP(Shader.block.getUniformLocation(1), true);
        //glUseProgram(0);
        
        
        if (Settings.getShadowMode() != 0)
        {
            glActiveTexture(GL_TEXTURE0 + 1);
            sunlight.bindTexture();
            glActiveTexture(GL_TEXTURE0);

            //glCullFace(GL_BACK);
            glEnable(GL_CULL_FACE);
            world.draw(DRAW_ACTUAL, mat, matSunlight);
            
            sunlight.unbindTexture();
        }
        else
        {
            world.draw(DRAW_ACTUAL, mat, null);
        }
        
        fb.unbind(GL_FRAMEBUFFER);
        
        glViewport(0, 0, width, height);
        
        glClearColor(0f, 0f, 0f, 0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        Shader.post.use();
        glUniform2f(Shader.post.getUniformLocation(0), width, height);
        glUniform1i(Shader.post.getUniformLocation(1), Util.random.nextInt());
        
        fb.bindTexture();
        fb.genMipmap();
        fb.draw(1 / xSamples, 1 / ySamples, true);
        glUseProgram(0);
        //sunlight.draw();
        //fb.bindTexture();
        //ModelLoader.basicModels.draw(matGUI, BasicModels.QUAD);
        //fb.unbindTexture();
        
        WorldOfCaves.checkGLErrors("Game.draw()");
    }
    
    public void destroy()
    {
        destroy(false);
    }
    public void destroy(boolean force)
    {
        WorldOfCaves.log("Destroying game...");
        
        if (force)
        {
            updater.forceExit();
        }
        else
        {
            updater.exit();
        }
        
        if (world != null)
        {
            world.destroy();
        }
        
        fb.destroy();
        
        if (Settings.getShadowMode() != 0)
        {
            sunlight.destroy();
        }
        
        isDestroyed = true;
        
        WorldOfCaves.checkGLErrors("Game.destroy()");
    }
    
    public MatrixHandler getMatrixHandler()
    {
        return mat;
    }
    
    public boolean isDestroyed()
    {
        return isDestroyed;
    }
}
