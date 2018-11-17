package worldofcaves.game.entity;

import worldofcaves.InputHandler;
import worldofcaves.Util;
import worldofcaves.game.Hitmask;
import worldofcaves.game.World;
import worldofcaves.game.block.Block;
import worldofcaves.game.block.BlockIntersection;

public class Player extends Mob
{
    private Block targetBlock;
    
    public Player(World world, double xPos, double yPos, double zPos)
    {
        super(world, xPos, yPos, zPos);
        
        eyeHeight = 1.7;
        hitmask = new Hitmask(-.25, -.25, 0, .25, .25, 1.8);
        
        /*jumpSpeed *= 3;
        maxSpeed *= 20;
        moveAcc *= 20;
        fallAcc *= 20;
        moveDec *= 20;
        fallDecFactor = 0;*/
    }
    
    @Override
    public short getId()
    {
        return PLAYER;
    }
    
    @Override
    public void update()
    {
        hAngle = Util.modD(hAngle + Math.toRadians(InputHandler.getMouseDX()), Math.PI * 2);
        vAngle = Util.clamp(vAngle - Math.toRadians(InputHandler.getMouseDY()), -Math.PI/2, Math.PI/2);
        
        int mf = 0, mr = 0;
        
        if (InputHandler.isKeyDown(InputHandler.KEY_FORWARD)) mf++;
        if (InputHandler.isKeyDown(InputHandler.KEY_BACKWARD)) mf--;
        if (InputHandler.isKeyDown(InputHandler.KEY_RIGHT)) mr++;
        if (InputHandler.isKeyDown(InputHandler.KEY_LEFT)) mr--;
        
        if (mf == 0 && mr == 0)
        {
            isMoving = false;
        }
        else
        {
            moveDir = (Math.atan2(mr, mf) + hAngle) % (Math.PI*2);
            isMoving = true;
        }
        
        isJumping = InputHandler.isKeyDown(InputHandler.KEY_JUMP);
        
        super.update();
        
        BlockIntersection bi = lookingAt();
        targetBlock = bi.block;
        
        if (InputHandler.wasKeyPressed(InputHandler.KEY_REMOVEBLOCK) && targetBlock.exists)
        {
            world.removeBlock(targetBlock, 256);
        }
        else if (InputHandler.wasKeyPressed(InputHandler.KEY_PLACEBLOCK))
        {
            int xx = targetBlock.x + bi.getFaceX();
            int yy = targetBlock.y + bi.getFaceY();
            int zz = targetBlock.z + bi.getFaceZ();
            Block b = world.getBlock(xx, yy, zz);
            
            if (b.exists && b.getId() == Block.AIR)
            {
                Block nb = new Block(Block.STONE, xx, yy, zz);
                Hitmask h = nb.getTranslatedHitmask();
                
                if (!Hitmask.collision(h, hitmask.translate(xPos, yPos, zPos)))
                {
                    world.setBlock(nb, 256);
                }
            }
        }
    }
    
    public Block getTargetBlock()
    {
        return targetBlock;
    }
}
