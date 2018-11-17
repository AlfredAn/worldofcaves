package worldofcaves.game;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import worldofcaves.model.ChunkModel;

public class ChunkUpdate extends UpdaterTask
{
    private final ChunkModel model;
    private int indexCount;
    private Updater updater;
    private int bufferId = -1;
    
    private ByteBuffer vBuffer;
    private IntBuffer iBuffer;
    
    public ChunkUpdate(float priority, ChunkModel model)
    {
        super(priority);
        
        this.model = model;
    }
    
    @Override
    public boolean execute(Updater updater)
    {
        synchronized (model.destroyLock)
        {
            if (model.isDestroyed())
            {
                return true;
            }
            
            this.updater = updater;

            bufferId = updater.assignBuffers();

            if (bufferId == -1)
            {
                return false;
            }

            vBuffer = updater.vertexBuffer(bufferId);
            iBuffer = updater.indexBuffer(bufferId);
            
            vBuffer.clear();
            iBuffer.clear();

            indexCount = model.renderBuffers(vBuffer, iBuffer);
            
            if (indexCount == 0)
            {
                free();
                bufferId = -1;
            }

            return true;
        }
    }
    
    @Override
    public void finish()
    {
        if (!model.isDestroyed())
        {
            if (indexCount == 0)
            {
                model.renderFinishEmpty();
            }
            else
            {
                model.renderFinish(vBuffer, iBuffer, indexCount);
            }
        }
        
        free();
    }
    
    @Override
    public void free()
    {
        if (bufferId != -1)
        {
            updater.freeBuffers(bufferId);
        }
    }
    
    public ChunkModel getModel()
    {
        return model;
    }
}