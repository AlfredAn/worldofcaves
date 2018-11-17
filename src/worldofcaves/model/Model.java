package worldofcaves.model;

import worldofcaves.WorldOfCaves;

public abstract class Model
{
    protected boolean isFinished = false;
    protected int vaoId, vboId, vboiId, indexCount;
    
    public abstract void render();
    //public abstract void draw(MatrixHandler mh);
    public abstract void destroy();
    
    @Override
    public void finalize()
    {
        if (isFinished)
        {
            WorldOfCaves.logError("Model not properly destroyed.");
            destroy();
        }
        
        try
        {
            super.finalize();
        }
        catch (Throwable e)
        {
            WorldOfCaves.logError("Error in Model.finalize()", e);
        }
    }
}
