package worldofcaves;

import worldofcaves.model.BasicModels;
import worldofcaves.model.BlockSelectBox;

public final class ModelLoader
{
    public static final BlockSelectBox blockSelectBox = new BlockSelectBox();
    public static final BasicModels basicModels = new BasicModels();
    
    private ModelLoader() {}
    
    public static void load()
    {
        WorldOfCaves.log("Loading models...");
        
        blockSelectBox.render();
        basicModels.render();
    }
    
    public static void unload()
    {
        WorldOfCaves.log("Unloading models...");
        
        blockSelectBox.destroy();
        basicModels.destroy();
    }
}
