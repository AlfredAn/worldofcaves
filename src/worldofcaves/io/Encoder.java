package worldofcaves.io;

public final class Encoder
{
    private Encoder() {}
    
    public static TagCompound encode(String compoundName, Encodable e)
    {
        TagCompound data = new TagCompound(compoundName);
        
        if (e instanceof Enumerable)
        {
            data.add(new TagIntV("id", ((Enumerable)e).getId()));
        }
        e.encode(data);
        
        return data;
    }
}