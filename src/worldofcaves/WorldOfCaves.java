package worldofcaves;

import worldofcaves.game.Game;
import java.io.File;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.glu.GLU;
import static org.lwjgl.opengl.GL11.*;
import worldofcaves.game.block.BlockDB;

public final class WorldOfCaves implements Runnable
{
    //!!!NOTE!!! Must be set to true to compile to standalone jar file
    public static final boolean isDist = false;
    
    private static boolean exit = false;
    private static boolean isForcingExit = false;
    
    private static Game game;
    
    public static void main(String[] args)
    {
        WorldOfCaves.log("World Of Caves starting up...");
        
        WorldOfCaves woc = new WorldOfCaves();
        woc.start();
    }
    
    private WorldOfCaves() {}
    
    private void start()
    {
        WorldOfCaves.log("Starting main thread...");
        
        Thread thread = new Thread(this, "Main Thread");
        thread.setPriority(7);
        thread.start();
    }
    
    @Override
    public void run()
    {
        mainLoop();
        
        WorldOfCaves.log("Execution finished.");
    }
    
    private static void mainLoop()
    {
        initLibraries();
        
        DisplayHandler.init();
        Settings.init();
        TextureLoader.load();
        Shader.load();
        ModelLoader.load();
        BlockDB.init();
        
        InputHandler.init();
        Timing.init();
        
        game = new Game();
        
        log("-----");
        log("Game started successfully!");
        log("-----");
        
        while (!exit)
        {
            update();
            draw();
            
            Display.update();
        }
        
        shutdown();
    }
    
    private static void update()
    {
        Timing.update();
        InputHandler.update();
        
        if (game != null)
        {
            game.update();
        }
        
        if (InputHandler.wasKeyPressed(InputHandler.KEY_EXIT) || Display.isCloseRequested())
        {
            exit();
        }
        else if (InputHandler.wasKeyPressed(InputHandler.KEY_FULLSCREEN))
        {
            DisplayHandler.toggleFullscreen();
        }
    }
    
    private static void draw()
    {
        if (game != null)
        {
            game.draw();
        }
        
        checkGLErrors("WorldOfCaves.draw()");
    }
    
    private static void shutdown()
    {
        shutdown(false);
    }
    private static void shutdown(boolean force)
    {
        log("Shutting down...");
        
        try
        {
            if (game != null)
            {
                game.destroy(force);
            }

            ModelLoader.unload();
            Shader.unload();
            TextureLoader.unload();

            log("Destroying display...");
            Display.destroy();
            
            log("Shutdown successful.");
        }
        catch (Exception e)
        {
            WorldOfCaves.logError("An error has occurred while shutting down.", e);
            System.exit(-1);
        }
    }
    
    public static void exit()
    {
        exit = true;
    }
    
    public static void checkGLErrors(String location)
    {
        int e = glGetError();
        
        if (e != 0)
        {
            Thread.dumpStack();
            forceExit("OpenGL error " + e + " at location \""
                    + location + "\": " + GLU.gluErrorString(e));
        }
    }
    
    public static void log(String s)
    {
        System.out.println(Util.getCurrentTimeStamp() + ": " + s);
    }
    
    public static void logError(String s)
    {
        logError(s, null);
    }
    public static void logError(String s, Throwable e)
    {
        if (s.equals(""))
        {
            s = "Unknown error.";
        }
        
        System.err.println(Util.getCurrentTimeStamp() + ": " + s);
        
        if (e != null)
        {
            e.printStackTrace();
        }
    }
    
    public static void forceExit()
    {
        forceExit("", null);
    }
    public static void forceExit(String errorText)
    {
        forceExit(errorText, null);
    }
    public static void forceExit(String errorText, Throwable e)
    {
        logError(errorText, e);
        log("Forcing exit...");
        
        if (!isForcingExit)
        {
            isForcingExit = true;
            shutdown(true);
        }
        else
        {
            logError("Unable to perform shutdown routine.");
        }
        
        System.exit(-1);
    }
    
    private static void initLibraries()
    {
        if (isDist)
        {
            log("Setting LWJGL library path...");
            
            String os = System.getProperty("os.name");
            String natives = "";
            
            if (os.contains("Windows"))
            {
                natives = "windows";
            }
            else if (os.contains("Mac"))
            {
                natives = "macosx";
            }
            else if (os.contains("Linux"))
            {
                natives = "linux";
            }
            else if (os.contains("Solaris"))
            {
                natives = "solaris";
            }
            else
            {
                logError("Error: OS not recognized.");
                System.exit(-1);
            }
            
            System.setProperty("org.lwjgl.librarypath", new File("native/" + natives).getAbsolutePath());
        }
    }
    
    /*
    static {
		String osName = System.getProperty("os.name");
		if ( osName.startsWith("Windows") )
			PLATFORM = Platform.WINDOWS;
		else if ( osName.startsWith("Linux") || osName.startsWith("FreeBSD") || osName.startsWith("SunOS") || osName.startsWith("Unix") )
			PLATFORM = Platform.LINUX;
		else if ( osName.startsWith("Mac OS X") || osName.startsWith("Darwin") )
			PLATFORM = Platform.MACOSX;
		else
			throw new LinkageError("Unknown platform: " + osName);
	}
    */
}