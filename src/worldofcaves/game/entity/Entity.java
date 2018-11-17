package worldofcaves.game.entity;

import java.util.ArrayList;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import worldofcaves.Timing;
import worldofcaves.Util;
import worldofcaves.WorldOfCaves;
import worldofcaves.game.Hitbox;
import worldofcaves.game.Hitmask;
import worldofcaves.game.World;
import worldofcaves.io.Enumerable;
import worldofcaves.io.Encodable;
import worldofcaves.io.TagCompound;
import worldofcaves.io.TagDoubleVec2;
import worldofcaves.io.TagDoubleVec3;

public abstract class Entity implements Encodable, Enumerable
{
    public static final short PLAYER = 0;
    
    private static final double maxZSpeed = 1000000;
    
    protected double xPos, yPos, zPos,
            xSpeed = 0, ySpeed = 0, zSpeed = -500,
            hAngle = 0, vAngle = 0,
            gravity = -25;
    protected int xStop = 0, yStop = 0, zStop = 0;
    protected World world;
    protected Hitmask hitmask = new Hitmask();
    
    public Entity(World world, double xPos, double yPos, double zPos)
    {
        this.world = world;
        
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
    }
    
    public void update()
    {
        zSpeed += gravity * Timing.getDelta();
        
        if (Math.abs(zSpeed) > maxZSpeed)
        {
            zSpeed = maxZSpeed * Math.signum(zSpeed);
        }
        
        double xm = xSpeed * Timing.getDelta();
        double ym = ySpeed * Timing.getDelta();
        double zm = zSpeed * Timing.getDelta();
        
        double m = Util.originDistance(xm, ym, zm);
        
        xStop = 0;
        yStop = 0;
        zStop = 0;
        
        if (m > 1)
        {
            double left = 1;
            
            while (true)
            {
                double pl = left;
                left -= 1. / m;
                
                if (left >= 0)
                {
                    if (!move(1. / m))
                    {
                        break;
                    }
                }
                else
                {
                    move(pl);
                    break;
                }
            }
        }
        else
        {
            move(1);
        }
    }
    
    private boolean move(double factor)
    {
        ArrayList<Hitbox> ignoreList = collisionList();
        
        /*if (ignoreList.size() > 0)
        {
            System.err.println(ignoreList.size());
        }*/
        
        if (xSpeed != 0 && xStop == 0)
        {
            double xp = xPos;
            xPos += xSpeed * Timing.getDelta() * factor;
            
            if (world.getBlockChunk((int)xPos, (int)yPos, (int)zPos) == null)
            {
                xSpeed = 0;
                xStop = (int)Math.signum(xSpeed);
                xPos = xp;
                return false;
            }
            
            ArrayList<Hitbox> cList = collisionList();
            
            for (int i = 0; i < hitmask.getAmount(); i++)
            {
                Hitbox h0 = hitmask.getHitbox(i);
                Hitbox h1 = h0.translate(xPos, yPos, zPos);
                
                if (cList.size() > 0)
                {
                    boolean ignore = true;
                    
                    for (int j = 0; j < cList.size(); j++)
                    {
                        Hitbox h2 = cList.get(j);
                        if (ignoreList.contains(h2))
                        {
                            continue;
                        }
                        
                        ignore = false;

                        if (xSpeed > 0)
                        {
                            if (h2.x1 < h1.x2)
                            {
                                xPos = h2.x1 - h0.x2;
                            }
                        }
                        else
                        {
                            if (h2.x2 > h1.x1)
                            {
                                xPos = h2.x2 - h0.x1;
                            }
                        }
                    }
                    
                    if (!ignore)
                    {
                        xStop = (int)Math.signum(xSpeed);
                        xSpeed = 0;
                    }
                }
            }
        }
        
        if (ySpeed != 0 && yStop == 0)
        {
            double yp = yPos;
            yPos += ySpeed * Timing.getDelta() * factor;
            
            if (world.getBlockChunk((int)xPos, (int)yPos, (int)zPos) == null)
            {
                ySpeed = 0;
                yStop = (int)Math.signum(ySpeed);
                yPos = yp;
                return false;
            }
            
            ArrayList<Hitbox> cList = collisionList();
            
            for (int i = 0; i < hitmask.getAmount(); i++)
            {
                Hitbox h0 = hitmask.getHitbox(i);
                Hitbox h1 = h0.translate(xPos, yPos, zPos);
                
                if (cList.size() > 0)
                {
                    boolean ignore = true;
                    
                    for (int j = 0; j < cList.size(); j++)
                    {
                        Hitbox h2 = cList.get(j);
                        if (ignoreList.contains(h2))
                        {
                            continue;
                        }
                        
                        ignore = false;

                        if (ySpeed > 0)
                        {
                            if (h2.y1 < h1.y2)
                            {
                                yPos = h2.y1 - h0.y2;
                            }
                        }
                        else
                        {
                            if (h2.y2 > h1.y1)
                            {
                                yPos = h2.y2 - h0.y1;
                            }
                        }
                    }
                    
                    if (!ignore)
                    {
                        yStop = (int)Math.signum(ySpeed);
                        ySpeed = 0;
                    }
                }
            }
        }
        
        if (zSpeed != 0 && zStop == 0)
        {
            double zp = zPos;
            zPos += zSpeed * Timing.getDelta() * factor;
            
            if (world.getBlockChunk((int)xPos, (int)yPos, (int)zPos) == null)
            {
                zSpeed = 0;
                zStop = (int)Math.signum(zSpeed);
                zPos = zp;
                return false;
            }
            
            ArrayList<Hitbox> cList = collisionList();
            
            //System.out.println(cList.size());
            
            for (int i = 0; i < hitmask.getAmount(); i++)
            {
                Hitbox h0 = hitmask.getHitbox(i);
                Hitbox h1 = h0.translate(xPos, yPos, zPos);
                
                if (cList.size() > 0)
                {
                    boolean ignore = true;
                    
                    for (int j = 0; j < cList.size(); j++)
                    {
                        Hitbox h2 = cList.get(j);
                        if (ignoreList.contains(h2))
                        {
                            continue;
                        }
                        
                        ignore = false;

                        if (zSpeed > 0)
                        {
                            if (h2.z1 < h1.z2)
                            {
                                zPos = h2.z1 - h0.z2;
                            }
                        }
                        else
                        {
                            if (h2.z2 > h1.z1)
                            {
                                zPos = h2.z2 - h0.z1;
                            }
                        }
                    }
                    
                    if (!ignore)
                    {
                        zStop = (int)Math.signum(zSpeed);
                        zSpeed = 0;
                    }
                }
            }
        }
        
        return (xStop != 0 || yStop != 0 || zStop != 0
                || xSpeed != 0 || ySpeed != 0 || zSpeed != 0);
    }
    
    public void tick() {}
    
    public void draw() {}
    
    public boolean isOnGround()
    {
        return zStop < 0;//collision(0, 0, -1./1024);
    }
    
    public boolean collision()
    {
        return collisionList().size() > 0;
    }
    public boolean collision(double dx, double dy, double dz)
    {
        return collisionList(dx, dy, dz).size() > 0;
    }
    
    public ArrayList<Hitbox> collisionList()
    {
        return collisionList(0, 0, 0);
    }
    public ArrayList<Hitbox> collisionList(double dx, double dy, double dz)
    {
        return hitmask.worldCollisionList(world, xPos + dx, yPos + dy, zPos + dz);
    }
    
    @Override
    public void encode(TagCompound data)
    {
        data.add(new TagDoubleVec3("pos", xPos, yPos, zPos));
        data.add(new TagDoubleVec3("speed", xSpeed, ySpeed, zSpeed));
        data.add(new TagDoubleVec2("rot", hAngle, vAngle));
    }
    
    public static Entity decode(TagCompound data, World world)
    {
        int id = data.findIntVAsInt("id", -1);
        
        if (id == -1)
        {
            WorldOfCaves.logError("Invalid entity data: Missing id tag");
            return null;
        }
        
        TagDoubleVec3 pos = data.findDoubleVec3Tag("pos");
        TagDoubleVec3 speed = data.findDoubleVec3Tag("speed");
        TagDoubleVec2 rot = data.findDoubleVec2Tag("rot");
        
        if (pos == null || speed == null || rot == null)
        {
            WorldOfCaves.logError("Invalid entity data: Missing pos/speed/rot tag");
            return null;
        }
        
        switch (id)
        {
            case PLAYER:
                Player p = new Player(world, pos.x, pos.y, pos.z);
                setVars(p, speed, rot);
                return p;
        }
        
        WorldOfCaves.logError("Invalid entity id: " + id);
        return null;
    }
    
    private static void setVars(Entity e, TagDoubleVec3 speed, TagDoubleVec2 rot)
    {
        e.setSpeed(speed.x, speed.y, speed.z);
        e.setAngle(rot.x, rot.y);
    }
    
    public void setPos(double xPos, double yPos, double zPos)
    {
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
    }
    
    public void setSpeed(double xSpeed, double ySpeed, double zSpeed)
    {
        this.xSpeed = xSpeed;
        this.ySpeed = ySpeed;
        this.zSpeed = zSpeed;
    }
    
    public void setAngle(double hAngle, double vAngle)
    {
        this.hAngle = hAngle;
        this.vAngle = vAngle;
    }
    
    public double getX()
    {
        return xPos;
    }
    
    public double getY()
    {
        return yPos;
    }
    
    public double getZ()
    {
        return zPos;
    }
    
    public double getXSpeed()
    {
        return xSpeed;
    }
    
    public double getYSpeed()
    {
        return ySpeed;
    }
    
    public double getZSpeed()
    {
        return zSpeed;
    }
    
    public double getHAngle()
    {
        return hAngle;
    }
    
    public double getVAngle()
    {
        return vAngle;
    }
    
    public float[] getFloatPos()
    {
        return new float[] {(float)xPos, (float)yPos, (float)zPos};
    }
    public double[] getPos()
    {
        return new double[] {xPos, yPos, zPos};
    }
    
    public double[] getSpeed()
    {
        return new double[] {xSpeed, ySpeed, zSpeed};
    }
    
    public double[] getAngle()
    {
        return new double[] {hAngle, vAngle};
    }
    
    public Vector3f getPosVector()
    {
        return new Vector3f((float)xPos, (float)yPos, (float)zPos);
    }
    
    public Vector3f getSpeedVector()
    {
        return new Vector3f((float)xSpeed, (float)ySpeed, (float)zSpeed);
    }
    
    public Vector2f getAngleVector()
    {
        return new Vector2f((float)hAngle, (float)vAngle);
    }
}











