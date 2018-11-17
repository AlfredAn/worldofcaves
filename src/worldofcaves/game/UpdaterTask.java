package worldofcaves.game;

public abstract class UpdaterTask implements Comparable<UpdaterTask>
{
    private boolean isFinished = false;
    private float priority;
    
    public UpdaterTask(float priority)
    {
        this.priority = priority;
    }
    
    public final boolean run(Updater updater)
    {
        if (isFinished)
        {
            throw new IllegalStateException("Task is already finished.");
        }
        
        boolean success = execute(updater);
        isFinished = success;
        
        return success;
    }
    
    public final void end()
    {
        if (!isFinished)
        {
            throw new IllegalStateException("Task isn't finished yet.");
        }
        
        finish();
    }
    
    protected abstract boolean execute(Updater updater);
    protected void finish() {}
    protected void free() {}
    
    public boolean isFinished()
    {
        return isFinished;
    }
    
    public void setPriority(float priority)
    {
        this.priority = priority;
    }
    
    public float getPriority()
    {
        return priority;
    }

    @Override
    public int compareTo(UpdaterTask ut)
    {
        float prio = ut.getPriority();
        return -Float.compare(priority, prio);
    }
}