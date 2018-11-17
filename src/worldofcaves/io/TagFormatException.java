package worldofcaves.io;

import java.io.IOException;

public class TagFormatException extends IOException
{
    public TagFormatException()
    {
        super();
    }
    
    public TagFormatException(String message)
    {
        super(message);
    }
    
    public TagFormatException(String message, Throwable cause)
    {
        super(message, cause);
    }
    
    public TagFormatException(Throwable cause)
    {
        super(cause);
    }
}