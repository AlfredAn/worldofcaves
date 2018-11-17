package worldofcaves.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import worldofcaves.WorldOfCaves;
import worldofcaves.game.Game;
import worldofcaves.game.World;
import static worldofcaves.io.IOHandler.*;

public final class WorldIO
{
    private WorldIO() {}
    
    public static World loadWorld(Game game, String name) throws IOException
    {
        return loadWorld(game, new File(savePath + "/" + name + ".wocw"));
    }
    public static World loadWorld(Game game, File file) throws IOException
    {
        WorldOfCaves.log("Loading world...");
        
        if (file.exists())
        {
            try (ZipFile zip = new ZipFile(file);)
            {
                ZipEntry e = zip.getEntry("world.wocd");
                
                if (e == null)
                {
                    throw new IOException("World contains no index.");
                }
                
                try (BufferedInputStream in = new BufferedInputStream(zip.getInputStream(e));)
                {
                    return loadWorldHeader(game, in);
                }
            }
        }
        
        throw new FileNotFoundException();
    }
    
    private static World loadWorldHeader(Game game, InputStream in) throws IOException
    {
        Tag t = Tag.read(in);
        
        System.out.println(t);
        if (t.getId() != Tag.TAG_COMPOUND)
        {
            throw new TagFormatException("Unable to load world: Missing TagCompound");
        }
        if (!t.name.equals("world"))
        {
            throw new TagFormatException("Unable to load world: TagCompound name must be \"world\"");
        }
        
        return World.decode((TagCompound)t, game);
    }
    
    public static void saveWorld(World world) throws IOException
    {
        saveWorld(world, new File(savePath + "/" + world.name + ".wocw"));
    }
    public static void saveWorld(World world, File file) throws IOException
    {
        WorldOfCaves.log("Saving world " + world.name + "...");
        
        checkFile(file);
        
        try (FileOutputStream os = new FileOutputStream(file);
                ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(os));)
        {
            ZipEntry e = new ZipEntry("world.wocd");
            zos.putNextEntry(e);
            saveWorldHeader(world, zos);
            zos.flush();
        }
    }
    
    private static void saveWorldHeader(World world, OutputStream os) throws IOException
    {
        TagCompound tc = Encoder.encode("world", world);
        tc.write(os);
    }
}
