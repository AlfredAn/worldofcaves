package worldofcaves;

public class Timer
{
    private long time;
    
    public Timer()
    {
        time = System.nanoTime();
    }
    
    public double delta()
    {
        long newTime = System.nanoTime();
        return (double)(newTime - time) / 1000000;
    }
    
    public void reset()
    {
        time = System.nanoTime();
    }
    
    public void printReset()
    {
        printDelta();
        reset();
    }
    public void printReset(String label)
    {
        printDelta(label);
        reset();
    }
    
    public void printDelta()
    {
        printDelta("Unknown");
    }
    public void printDelta(String label)
    {
        WorldOfCaves.log(label + ": " + delta() + " ms");
    }
}
