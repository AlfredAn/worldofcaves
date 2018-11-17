package worldofcaves.game.block;

public class BlockIntersection
{
    public static final byte
            FACE_NONE = -1,
            FACE_WEST = 0,   //-x
            FACE_EAST = 1,   //+x
            FACE_SOUTH = 2,  //-y
            FACE_NORTH = 3,  //+y
            FACE_BOTTOM = 4, //-z
            FACE_TOP = 5;    //+z
    
    public final Block block;
    public final byte face;
    public final float x, y, z;
    
    public BlockIntersection()
    {
        this(new Block(Block.NONE), FACE_NONE, 0, 0, 0);
    }
    public BlockIntersection(Block block, byte face, float x, float y, float z)
    {
        if (face < -1 || face > 5)
        {
            throw new IllegalArgumentException("Invalid face value: " + face);
        }
        
        this.block = block;
        this.face = face;
        
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public int getFaceX()
    {
        return getFaceX(face);
    }
    
    public int getFaceY()
    {
        return getFaceY(face);
    }
    
    public int getFaceZ()
    {
        return getFaceZ(face);
    }
    
    public static int getFaceX(byte face)
    {
        switch (face)
        {
            case FACE_EAST:
                return 1;
            case FACE_WEST:
                return -1;
        }
        return 0;
    }
    
    public static int getFaceY(byte face)
    {
        switch (face)
        {
            case FACE_NORTH:
                return 1;
            case FACE_SOUTH:
                return -1;
        }
        return 0;
    }
    
    public static int getFaceZ(byte face)
    {
        switch (face)
        {
            case FACE_TOP:
                return 1;
            case FACE_BOTTOM:
                return -1;
        }
        return 0;
    }
    
    public String getFaceName()
    {
        return getFaceName(face);
    }
    
    public static String getFaceName(byte face)
    {
        if (face < -1 || face > 5)
        {
            throw new IllegalArgumentException("Invalid face value: " + face);
        }
        
        switch (face)
        {
            case FACE_EAST:
                return "east";
            case FACE_WEST:
                return "west";
            case FACE_NORTH:
                return "north";
            case FACE_SOUTH:
                return "south";
            case FACE_TOP:
                return "top";
            case FACE_BOTTOM:
                return "bottom";
        }
        
        return "error";
    }
    
    @Override
    public String toString()
    {
        return block.toString() + "\nface = " + getFaceName(face)
                 + "\nix = " + x
                 + "\niy = " + y
                 + "\niz = " + z;
    }
}
