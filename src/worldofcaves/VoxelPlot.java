package worldofcaves;

import org.lwjgl.util.vector.Vector3f;
import worldofcaves.game.Hitmask;
import worldofcaves.game.World;
import worldofcaves.game.block.Block;
import worldofcaves.game.block.BlockIntersection;

public class VoxelPlot
{
    private final Vector3f size = new Vector3f();
    private final Vector3f off = new Vector3f();
    private final Vector3f pos = new Vector3f();
    private final Vector3f dir = new Vector3f();
    
    private int idX = 0, idY = 0, idZ = 0;
    
    private final Vector3f delta = new Vector3f();
    private int signX = 0, signY = 0, signZ = 0;
    private final Vector3f max = new Vector3f();
    
    private int limit;
    private int plotted;
    
    public VoxelPlot(float xOff, float yOff, float zOff,
            float xSize, float ySize, float zSize)
    {
        off.set(xOff, yOff, zOff);
        size.set(xSize, ySize, zSize);
    }
    
    public void plot(Vector3f position, Vector3f direction, int cells)
    {
        limit = cells;
        
        pos.set(position);
        direction.normalise(dir);
        
        delta.set(size);
        delta.x /= dir.x;
        delta.y /= dir.y;
        delta.z /= dir.z;
        
        signX = (dir.x > 0) ? 1 : (dir.x < 0 ? -1 : 0);
        signY = (dir.y > 0) ? 1 : (dir.y < 0 ? -1 : 0);
        signZ = (dir.z > 0) ? 1 : (dir.z < 0 ? -1 : 0);
        
        reset();
    }
    
    public boolean next()
    {
        if (plotted++ > 0)
        {
            float mx = (float)signX * max.x;
            float my = (float)signY * max.y;
            float mz = (float)signZ * max.z;
            
            if (mx < my && mx < mz)
            {
                max.x += delta.x;
                idX += signX;
            }
            else if (mz < my && mz < mx)
            {
                max.z += delta.z;
                idZ += signZ;
            }
            else
            {
                max.y += delta.y;
                idY += signY;
            }
        }
        
        return (plotted <= limit);
    }
    
    public void reset()
    {
        plotted = 0;
        
        idX = (int)Math.floor((pos.x - off.x) / size.x);
        idY = (int)Math.floor((pos.y - off.y) / size.y);
        idZ = (int)Math.floor((pos.z - off.z) / size.z);
        
        float ax = (float)idX * size.x + off.x;
        float ay = (float)idY * size.y + off.y;
        float az = (float)idZ * size.z + off.z;
        
        max.x = (signX > 0) ? ax + size.x - pos.x : pos.x - ax;
        max.y = (signY > 0) ? ay + size.y - pos.y : pos.y - ay;
        max.z = (signZ > 0) ? az + size.z - pos.z : pos.z - az;
        
        max.x /= dir.x;
        max.y /= dir.y;
        max.z /= dir.z;
    }
    
    public void end()
    {
        plotted = limit + 1;
    }
    
    private static Vector3f iPoint;
    
    private static boolean raySquareIntersect(
            Vector3f R1, Vector3f R2,
            Vector3f S1, Vector3f S2, Vector3f S3)
    {
        Vector3f dS21 = Vector3f.sub(S2, S1, null);
        Vector3f dS31 = Vector3f.sub(S3, S1, null);
        Vector3f n = Vector3f.cross(dS21, dS31, null);
        
        Vector3f dR = Vector3f.sub(R1, R2, null);
        
        float ndotdR = Vector3f.dot(n, dR);
        
        if (Math.abs(ndotdR) < 1f/65536)
        {
            return false;
        }
        
        float t = -Vector3f.dot(n, Vector3f.sub(R1, S1, null)) / ndotdR;
        Vector3f M = Vector3f.add(R1, (Vector3f)dR.scale(t), null);
        iPoint = M;
        
        Vector3f dMS1 = Vector3f.sub(M, S1, null);
        float u = Vector3f.dot(dMS1, dS21);
        float v = Vector3f.dot(dMS1, dS31);
        
        return (u >= 0 && u <= Vector3f.dot(dS21, dS21)
             && v >= 0 && v <= Vector3f.dot(dS31, dS31));
    }
    
    private static BlockIntersection blockIntersection(
            Block b,
            Vector3f pos,
            Vector3f dir,
            float reach)
    {
        Hitmask h = b.getTranslatedHitmask();
        float x1 = (float)h.getX1();
        float y1 = (float)h.getY1();
        float z1 = (float)h.getZ1();
        float x2 = (float)h.getX2();
        float y2 = (float)h.getY2();
        float z2 = (float)h.getZ2();
        Vector3f r1 = new Vector3f(pos);
        Vector3f r2 = Vector3f.add(pos, dir, null);
        iPoint = new Vector3f();
        Vector3f rPoint;
        
        byte face = BlockIntersection.FACE_NONE;
        float dist, minDist = reach * reach;
        
        // side x1
        if (raySquareIntersect(r1, r2,
                v(x1, y1, z1),
                v(x1, y2, z1),
                v(x1, y1, z2)))
        {
            dist = Util.pointDistanceSqr(r1, iPoint);
            
            if (dist < minDist)
            {
                face = BlockIntersection.FACE_WEST;
                minDist = dist;
            }
        }
        // side y1
        if (raySquareIntersect(r1, r2,
                v(x1, y1, z1),
                v(x2, y1, z1),
                v(x1, y1, z2)))
        {
            dist = Util.pointDistanceSqr(r1, iPoint);
            
            if (dist < minDist)
            {
                face = BlockIntersection.FACE_SOUTH;
                minDist = dist;
            }
        }
        // side z1
        if (raySquareIntersect(r1, r2,
                v(x1, y1, z1),
                v(x2, y1, z1),
                v(x1, y2, z1)))
        {
            dist = Util.pointDistanceSqr(r1, iPoint);
            
            if (dist < minDist)
            {
                face = BlockIntersection.FACE_BOTTOM;
                minDist = dist;
            }
        }
        // side x2
        if (raySquareIntersect(r1, r2,
                v(x2, y1, z1),
                v(x2, y2, z1),
                v(x2, y1, z2)))
        {
            dist = Util.pointDistanceSqr(r1, iPoint);
            
            if (dist < minDist)
            {
                face = BlockIntersection.FACE_EAST;
                minDist = dist;
            }
        }
        // side y2
        if (raySquareIntersect(r1, r2,
                v(x1, y2, z1),
                v(x2, y2, z1),
                v(x1, y2, z2)))
        {
            dist = Util.pointDistanceSqr(r1, iPoint);
            
            if (dist < minDist)
            {
                face = BlockIntersection.FACE_NORTH;
                minDist = dist;
            }
        }
        // side z2
        if (raySquareIntersect(r1, r2,
                v(x1, y1, z2),
                v(x2, y1, z2),
                v(x1, y2, z2)))
        {
            dist = Util.pointDistanceSqr(r1, iPoint);
            
            if (dist < minDist)
            {
                face = BlockIntersection.FACE_TOP;
                minDist = dist;
            }
        }
        
        if (face == BlockIntersection.FACE_NONE)
        {
            return new BlockIntersection();
        }
        
        return new BlockIntersection(b, face, iPoint.x, iPoint.y, iPoint.z);
    }
    
    private static Vector3f v(float x, float y, float z)
    {
        return new Vector3f(x, y, z);
    }
    
    public static BlockIntersection lookingAt(World world, double xPos, double yPos, double zPos, double hAngle, double vAngle, double reach)
    {
        Vector3f dir = new Vector3f(0, 1, 0);
        
        dir = Util.rotateVector(dir, vAngle, 0, hAngle);
        
        VoxelPlot plotter = new VoxelPlot(0, 0, 0, 1, 1, 1);
        
        plotter.plot(new Vector3f((float)xPos, (float)yPos, (float)zPos),
                dir, (int)(reach * 2 + 1));
        
        Block selected = null;
        
        while (plotter.next())
        {
            int x = plotter.getX();
            int y = plotter.getY();
            int z = plotter.getZ();
            
            Block b = world.getBlock(x, y, z);
            
            if (b.isSelectable())
            {
                selected = b;
                break;
            }
        }
        
        if (selected == null)
        {
            return new BlockIntersection();
        }
        
        Vector3f pos = new Vector3f((float)xPos, (float)yPos, (float)zPos);
        
        BlockIntersection bi = blockIntersection(selected, pos, dir, (float)reach);
        //System.out.println(bi);
        //Vector3f v = new Vector3f();
        //BlockIntersection bi = new BlockIntersection(selected, BlockIntersection.FACE_TOP, v.x, v.y, v.z);
        
        return bi;
    }
    
    public int getX()
    {
        return idX;
    }
    
    public int getY()
    {
        return idY;
    }
    
    public int getZ()
    {
        return idZ;
    }
    
    public Vector3f actual()
    {
        return new Vector3f((float)idX * size.x + off.x,
                (float)idY * size.y + off.y,
                (float)idZ * size.z + off.z);
    }
    
    public Vector3f size()
    {
        return size;
    }
    
    public void size(float xSize, float ySize, float zSize)
    {
        size.set(xSize, ySize, zSize);
    }
    
    public Vector3f offset()
    {
        return off;
    }
    
    public void offset(float x, float y, float z)
    {
        off.set(x, y, z);
    }
    
    public Vector3f position()
    {
        return pos;
    }
    
    public Vector3f direction()
    {
        return dir;
    }
    
    public int signX()
    {
        return signX;
    }
    
    public int signY()
    {
        return signY;
    }
    
    public int signZ()
    {
        return signZ;
    }
    
    public Vector3f delta()
    {
        return delta;
    }
    
    public Vector3f max()
    {
        return max;
    }
    
    public int limit()
    {
        return limit;
    }
    
    public int plotted()
    {
        return plotted;
    }
}