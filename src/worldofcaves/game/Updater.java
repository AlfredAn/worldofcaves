package worldofcaves.game;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.PriorityQueue;
import org.lwjgl.BufferUtils;
import worldofcaves.WorldOfCaves;
import worldofcaves.model.ChunkModel;

public class Updater implements Runnable
{
    private final Thread thread;
    private final Game game;
    
    private final ArrayList<UpdaterTask> taskList = new ArrayList<>();
    private final PriorityQueue<UpdaterTask> tasks = new PriorityQueue<>();
    private final ArrayList<UpdaterTask> finishedTasks = new ArrayList<>();
    
    private static final int maxBuffers = 12;
    private final ByteBuffer[] vBuffers = new ByteBuffer[maxBuffers];
    private final IntBuffer[] iBuffers = new IntBuffer[maxBuffers];
    private final boolean[] bufferOccupied = new boolean[maxBuffers];
    private int buffersRemaining = maxBuffers;
    
    private boolean exit = false, forceExit = false;
    
    public Updater(Game game)
    {
        WorldOfCaves.log("Starting updater thread...");
        this.game = game;
        
        thread = new Thread(this, "Chunk Updater");
        thread.setPriority(6);
        thread.setDaemon(true);
        thread.start();
    }
    
    @Override
    public void run()
    {
        for (int i = 0; i < maxBuffers; i++)
        {
            vBuffers[i] = BufferUtils.createByteBuffer(ChunkModel.vboSize);
            iBuffers[i] = BufferUtils.createIntBuffer(ChunkModel.vboiSize / 4);
            bufferOccupied[i] = false;
        }
        
        while (!exit)
        {
            if (tasks.isEmpty())
            {
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {}
            }
            
            if (forceExit)
            {
                return;
            }
            
            while (true)
            {
                if (forceExit)
                {
                    return;
                }
                
                UpdaterTask t;
                
                synchronized (tasks)
                {
                    if (tasks.isEmpty())
                    {
                        break;
                    }
                    
                    t = tasks.poll();
                    
                    if (t instanceof ChunkUpdate)
                    {
                        taskList.remove(t);
                    }
                }
                
                boolean success = t.run(this);
                
                boolean e = exit;
                if (e)
                {
                    t.free();
                }
                
                if (success)
                {
                    if (!e)
                    {
                        synchronized (finishedTasks)
                        {
                            finishedTasks.add(t);
                        }
                    }
                }
                else
                {
                    synchronized (tasks)
                    {
                        addTask(t, false);
                    }
                }
            }
            
            if (forceExit)
            {
                return;
            }
        }
    }
    
    public void addTask(UpdaterTask task)
    {
        addTask(task, true);
    }
    public void addTask(UpdaterTask task, boolean interrupt)
    {
        synchronized (tasks)
        {
            if (task instanceof ChunkUpdate)
            {
                ChunkUpdate cu1 = (ChunkUpdate)task;
                ChunkModel cm1 = cu1.getModel();
                //Vector3i key = cm1.chunk.pos;
                UpdaterTask ut = null;
                int id = -1;
                
                for (int i = 0; i < taskList.size(); i++)
                {
                    UpdaterTask t = taskList.get(i);
                    if (t instanceof ChunkUpdate)
                    {
                        ut = t;
                        id = i;
                        break;
                    }
                }
                
                if (ut != null)
                {
                    ChunkUpdate cu2 = (ChunkUpdate)ut;
                    ChunkModel cm2 = cu2.getModel();

                    if (cm1 == cm2)
                    {
                        float prio = cu2.getPriority() + task.getPriority();
                        task.setPriority(prio);

                        tasks.remove(ut);
                        taskList.remove(id);
                    }
                }
                
                taskList.add(task);
            }
            
            tasks.add(task);
        }
        
        if (interrupt)
        {
            interrupt();
        }
    }
    
    public void finish()
    {
        synchronized (finishedTasks)
        {
            for (int i = 0; i < finishedTasks.size(); i++)
            {
                finishedTasks.get(i).end();
            }
            
            finishedTasks.clear();
        }
    }
    
    public int queueSize()
    {
        synchronized (tasks)
        {
            return tasks.size();
        }
    }
    
    public int assignBuffers()
    {
        if (buffersRemaining == 0)
        {
            return -1;
        }
        
        for (int i = 0; i < maxBuffers; i++)
        {
            if (bufferOccupied[i])
            {
                continue;
            }
            
            bufferOccupied[i] = true;
            buffersRemaining--;
            return i;
        }
        
        return -1;
    }
    
    public void freeBuffers(int id)
    {
        bufferOccupied[id] = false;
        vBuffers[id].clear();
        iBuffers[id].clear();
        buffersRemaining++;
    }
    
    public int buffersRemaining()
    {
        return buffersRemaining;
    }
    
    public ByteBuffer vertexBuffer(int id)
    {
        return vBuffers[id];
    }
    
    public IntBuffer indexBuffer(int id)
    {
        return iBuffers[id];
    }
    
    public void interrupt()
    {
        thread.interrupt();
    }
    
    public void exit()
    {
        exit = true;
        WorldOfCaves.log("Stopping updater thread...");
        interrupt();
        
        /*try
        {
            thread.join(10000);
        }
        catch (InterruptedException e) {}
        
        if (thread.isAlive())
        {
            WorldOfCaves.logError("Unable to shutdown updater thread.");
            forceExit();
        }*/
    }
    
    public void forceExit()
    {
        WorldOfCaves.log("Forcing shutdown of updater thread...");
        exit = true;
        forceExit = true;
        interrupt();
        
        /*try
        {
            thread.join(10000);
        }
        catch (InterruptedException e) {}
        
        if (thread.isAlive())
        {
            WorldOfCaves.logError("Unable to shutdown updater thread.");
        }*/
    }
}
