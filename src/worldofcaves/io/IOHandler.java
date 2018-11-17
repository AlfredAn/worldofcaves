package worldofcaves.io;

import java.io.File;
import java.io.IOException;

public final class IOHandler
{
    private IOHandler() {}
    
    static final String gamePath = System.getenv("AppData")
            + "/Alfred Andersson/World Of Caves";
    
    static final String savePath = gamePath + "/save";
    
    //static final File gameFolder = new File(gamePath);
    //static final File saveFolder = new File(savePath);
    
    protected static boolean checkFolder(File dir) throws IOException
    {
        if (!dir.exists())
        {
            if (dir.mkdirs())
            {
                return true;
            }
            else
            {
                throw new IOException("Unable to create directory.");
            }
        }
        
        return false;
    }
    
    protected static boolean checkFile(File file) throws IOException
    {
        File parent = file.getParentFile();
        
        if (parent != null)
        {
            checkFolder(parent);
        }
        
        return file.createNewFile();
    }
    
    public static String getGamePath()
    {
        return gamePath;
    }
    
    public static String getSavePath()
    {
        return savePath;
    }
}