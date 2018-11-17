package worldofcaves.game.entity;

import org.lwjgl.util.vector.Matrix4f;
import worldofcaves.MatrixHandler;
import worldofcaves.Timing;
import worldofcaves.VoxelPlot;
import worldofcaves.game.World;
import worldofcaves.game.block.BlockIntersection;
import worldofcaves.io.TagCompound;

public abstract class Mob extends Entity
{
    protected double
            eyeHeight = 1.7,
            
            maxSpeed = 3.5,
            moveAcc = 32,
            moveDec = 32,
            
            fallAcc = 8,
            fallDec = 0,
            fallDecFactor = .25,
            
            jumpSpeed = 8,
            
            moveDir = 0,
            
            reach = 5;
    protected boolean isMoving = false, isJumping = false;
    
    public Mob(World world, double xPos, double yPos, double zPos)
    {
        super(world, xPos, yPos, zPos);
    }
    
    @Override
    public void update()
    {
        double acc, dec;
        boolean onGround = isOnGround();
        
        if (onGround)
        {
            acc = moveAcc;
            dec = moveDec;
        }
        else
        {
            acc = fallAcc;
            dec = fallDec;
        }
        
        if (isMoving)
        {
            double ax = -Math.cos(moveDir + Math.PI/2);
            double ay = Math.sin(moveDir + Math.PI/2);
            
            xSpeed += acc * ax * Timing.getDelta();
            ySpeed += acc * ay * Timing.getDelta();
            
            if (yStop != 0 && ax != 0)
            {
                if (ax > 0)
                {
                    xSpeed = Math.min(xSpeed, ax * maxSpeed);
                }
                else
                {
                    xSpeed = Math.max(xSpeed, ax * maxSpeed);
                }
            }
            
            if (xStop != 0 && ay != 0)
            {
                if (ay > 0)
                {
                    ySpeed = Math.min(ySpeed, ay * maxSpeed);
                }
                else
                {
                    ySpeed = Math.max(ySpeed, ay * maxSpeed);
                }
            }
            
            double l = Math.sqrt(xSpeed * xSpeed + ySpeed * ySpeed);
            
            if (l > maxSpeed)
            {
                double f = l / maxSpeed;
                xSpeed /= f;
                ySpeed /= f;
            }
        }
        else
        {
            double l = Math.sqrt(xSpeed * xSpeed + ySpeed * ySpeed);
            
            if (l != 0)
            {
                double newSp = Math.max(l - dec * Timing.getDelta(), 0);

                if (newSp == 0)
                {
                    xSpeed = 0;
                    ySpeed = 0;
                }
                else
                {
                    xSpeed *= newSp / l;
                    ySpeed *= newSp / l;
                    
                    if (!onGround)
                    {
                        xSpeed *= Math.pow(fallDecFactor, Timing.getDelta());
                        ySpeed *= Math.pow(fallDecFactor, Timing.getDelta());
                    }
                }
            }
        }
        
        if (isJumping && onGround && zSpeed < 1./256)
        {
            zSpeed = jumpSpeed;
        }
        
        super.update();
    }
    
    public BlockIntersection lookingAt()
    {
        return VoxelPlot.lookingAt(world, xPos, yPos, zPos + eyeHeight, -hAngle, -vAngle, reach);
    }
    
    public Matrix4f getViewMatrix(Matrix4f dest)
    {
        return MatrixHandler.viewMatrix((float)xPos, (float)yPos, (float)(zPos + eyeHeight),
                (float)(vAngle - Math.PI/2), 0, (float)hAngle, dest);
    }
    
    public double getEyeHeight()
    {
        return eyeHeight;
    }
    
    public double getReach()
    {
        return reach;
    }
}
