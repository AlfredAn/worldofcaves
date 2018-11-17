package worldofcaves.game;

import java.io.IOException;
import worldofcaves.game.generator.WorldGenerator;
import worldofcaves.MatrixHandler;
import worldofcaves.game.entity.Player;
import worldofcaves.game.entity.Entity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import worldofcaves.ModelLoader;
import worldofcaves.TextureLoader;
import worldofcaves.Util;
import worldofcaves.WorldOfCaves;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import worldofcaves.Settings;
import worldofcaves.Shader;
import worldofcaves.Timing;
import worldofcaves.Vector3i;
import worldofcaves.game.block.Block;
import worldofcaves.io.TagFormatException;
import worldofcaves.io.Encodable;
import worldofcaves.io.Encoder;
import worldofcaves.io.TagCompound;
import worldofcaves.io.TagInt;
import worldofcaves.io.TagLong;
import worldofcaves.io.TagString;
import worldofcaves.io.WorldIO;
import worldofcaves.model.BasicModels;
import worldofcaves.model.ChunkModel;

public final class World implements Encodable
{
    private final ArrayList<Chunk> chunkList, chunkSunlightList;
    private final HashMap<Vector3i, Chunk> chunkMap;
    
    private final ArrayList<Entity> entities;
    private final ArrayList<ChunkModel> chunkUpdates;
    
    public final Game game;
    public final String name;
    
    private Player player = null;
    private final WorldGenerator wg;
    
    private long currentTick;
    private int fracTick;
    
    private boolean isInitialized = false;
    
    public static final double tickRate = 30, maxFrameTicks = 10;
    public static final int tickNano = (int)(1000000000 / tickRate);
    
    private static final float zPrioBiasBase = 2, zRenderDistBias = 0.5f;
    private static float zPrioBias = 2;
    
    public static final float renderDistBase = 512;
    private static final float renderDistAdd = (float)(Math.sqrt(Util.sqr(Chunk.xSize) + Util.sqr(Chunk.ySize) + Util.sqr(Chunk.zSize)));
    public static final float renderDist = renderDistBase + renderDistAdd;
    private static final float renderDistZ = (renderDistBase * zRenderDistBias) + renderDistAdd;
    private static final float renderDistZFactor = renderDistZ / renderDist;
    private static final double chunkLoadThreshold = 1.1;
    
    private static final float minPriority = (float)(1. / Util.sqr(renderDist * 1.5));
    
    private static final int startChunks = 32,
            maxChunks = (int)(4. / 3 * Math.PI * renderDistBase * renderDistBase * renderDistBase * zRenderDistBias / Math.pow(Chunk.xSize, 3)),
            chunksPerFrame = 8, maxQueueSize = 256;
    
    public World(Game game, String name, WorldGenerator wg)
    {
        this(game, name, wg, null);
    }
    public World(Game game, String name, WorldGenerator wg, Player player)
    {
        this(game, name, wg, player, true);
    }
    private World(Game game, String name, WorldGenerator wg, Player player, boolean init)
    {
        WorldOfCaves.log("Creating world...");
        
        this.game = game;
        this.name = name;
        
        entities = new ArrayList<>();
        
        this.player = player;
        this.wg = wg;
        
        chunkUpdates = new ArrayList<>(chunksPerFrame + 16);
        
        chunkList = new ArrayList<>(maxChunks);
        chunkSunlightList = new ArrayList<>(maxChunks);
        chunkMap = new HashMap<>((int)Math.ceil(maxChunks / 0.75));
        
        currentTick = 4500;
        fracTick = 0;
        
        if (init)
        {
            init();
        }
    }
    
    private void init()
    {
        if (player == null)
        {
            player = new Player(this, 0, 0, 128);
        }
        
        addEntity(player);
        
        for (int i = 0; i < startChunks; i++)
        {
            loadNext();
        }
        
        isInitialized = true;
    }
    
    private void addEntity(Entity e)
    {
        if (e == null)
        {
            throw new NullPointerException("Entity e cannot be null.");
        }
        
        entities.add(e);
    }
    
    private void removeEntity(Entity e)
    {
        entities.remove(e);
    }
    
    private void addChunk(Chunk c)
    {
        if (chunkExists(c.pos))
        {
            throw new IllegalStateException("Two chunks cannot have the same position.");
        }
        if (c == null)
        {
            throw new NullPointerException();
        }
        
        chunkList.add(c);
        chunkSunlightList.add(c);
        chunkMap.put(c.pos, c);
    }
    
    private void removeChunk(Chunk c)
    {
        boolean l = chunkList.contains(c);
        boolean m = chunkMap.containsKey(c.pos);
        
        if (!l || !m)
        {
            System.out.println(chunkMap);
            System.out.println(c);
            System.out.println("l = " + l + "\nm = " + m);
            throw new IllegalArgumentException("Chunk c isn't in the list.");
        }
        
        chunkList.remove(c);
        chunkSunlightList.remove(c);
        chunkMap.remove(c.pos);
        
        c.updateAdjacent(c.getModel(), 1f);
        
        c.destroy();
    }
    
    public void chunkUpdate(ChunkModel cm)
    {
        chunkUpdate(cm, 1, false);
    }
    public void chunkUpdate(ChunkModel cm, float priority)
    {
        chunkUpdate(cm, priority, false);
    }
    public void chunkUpdate(ChunkModel cm, float priority, boolean abs)
    {
        if (chunkUpdates.contains(cm))
        {
            cm.setPriority(cm.getPriority() + priority, true);
            return;
        }
        
        cm.setPriority(priority, abs);
        chunkUpdates.add(cm);
    }
    
    public void chunkUpdate(ArrayList<ChunkModel> cm)
    {
        chunkUpdate(cm, 1, false);
    }
    public void chunkUpdate(ArrayList<ChunkModel> cm, float priority)
    {
        chunkUpdate(cm, priority, false);
    }
    public void chunkUpdate(ArrayList<ChunkModel> cm, float priority, boolean abs)
    {
        chunkUpdate(cm, priority, abs, false);
    }
    public void chunkUpdate(ArrayList<ChunkModel> cm, float priority, boolean abs, boolean invertPrio)
    {
        if (invertPrio)
        {
            float maxPrio = Float.NEGATIVE_INFINITY;
            
            for (int i = 0; i < cm.size(); i++)
            {
                float prio = cm.get(i).getPriority();
                if (prio > maxPrio)
                {
                    maxPrio = prio;
                }
            }
            
            for (int i = 0; i < cm.size(); i++)
            {
                ChunkModel cmm = cm.get(i);
                chunkUpdate(cmm, maxPrio * 2 - cmm.getPriority(), abs);
            }
        }
        else
        {
            for (int i = 0; i < cm.size(); i++)
            {
                chunkUpdate(cm.get(i), priority, abs);
            }
        }
    }
    
    private void renderModels(ArrayList<ChunkModel> models)
    {
        for (int i = 0; i < models.size(); i++)
        {
            ChunkModel cm = models.get(i);
            float prio = cm.getPriority();
            
            ChunkUpdate cu = new ChunkUpdate(prio, cm);
            game.updater.addTask(cu);
        }
    }
    
    public void update()
    {
        if (!isInitialized)
        {
            throw new IllegalStateException("This world isn't initialized yet.");
        }
        
        fracTick += Timing.getDeltaNano();
        
        for (int i = 0; i < maxFrameTicks && fracTick >= tickNano; i++)
        {
            tick();
            currentTick++;
            fracTick -= tickNano;
        }
        
        zPrioBias = zPrioBiasBase / (64 + Util.abs((float)player.getSpeed()[2])) * 64;
        
        int qs = game.updater.queueSize();
        
        int load = Util.min(maxQueueSize - qs, chunksPerFrame);
        
        if (load > 0)
        {
            Vector3i[] next = getNextChunks(load);
            
            load = next.length;
            
            for (int i = 0; i < load; i++)
            {
                if (chunkList.size() < maxChunks)
                {
                    loadChunk(next[i]);
                }
                else
                {
                    Chunk cf = getFarthestChunk();
                    if (cf == null) break;
                    Vector3i cn = next[i];
                    if (cn == null) break;
                    
                    float priof = cf.getPriority();
                    float prion = getChunkLoadPriority(cn);

                    if (prion > priof * chunkLoadThreshold)
                    {
                        removeChunk(cf);
                        loadChunk(cn.x, cn.y, cn.z);
                    }
                    else
                    {
                        break;
                    }
                }
            }
        }
        
        //getFarthestChunk(true);
        
        for (int i = 0; i < entities.size(); i++)
        {
            entities.get(i).update();
        }
        
        renderModels(chunkUpdates);
        chunkUpdates.clear();
    }
    
    public long getCurrentTick()
    {
        return currentTick;
    }
    
    public int getFracTick()
    {
        return fracTick;
    }
    
    public double getCurrentTickD()
    {
        return (double)currentTick + (double)fracTick / tickNano;
    }
    
    public double getSunAngle()
    {
        return ((getCurrentTickD() / 36000) % 1) * Math.PI * 2;
    }
    
    private static final float[] baseDay = new float[] {.4f, .6f, .9f};
    private static final float[] baseNight = new float[] {.35f, .45f, .5f};
    
    public float[] getSkyColor(float[] dest)
    {
        if (dest == null)
        {
            dest = new float[3];
        }
        
        float sh = (float)Math.sin(getSunAngle());
        float ah = (float)Math.pow(1 - Math.min(Math.abs(sh-.025), 1), 5);
        float f = Util.mix(.0625f, 1f, Util.clamp(sh, 0, 1)) / 0.75f;
        
        float[] base;
        if (sh > 0)
        {
            base = baseDay;
        }
        else
        {
            base = baseNight;
        }
        //.996, .685, .663
        dest[0] = Util.mix(base[0], .996f, ah*2) * f;
        dest[1] = Util.mix(base[1], .685f, ah*2) * f;
        dest[2] = Util.mix(base[2], .663f, ah*2) * f;
        
        //System.out.println(Util.coordString(dest[0], dest[1], dest[2]));
        
        return dest;
    }
    
    public void tick()
    {
        if (currentTick % 30 == 0)
        {
            for (int i = 0; i < chunkList.size(); i++)
            {
                chunkList.get(i).updatePriority();
            }
        }
        
        if (currentTick % 1800 == 450)
        {
            Collections.sort((List<Chunk>)chunkList);
        }
        if (currentTick % 1800 == 1350)
        {
            Collections.sort((List<Chunk>)chunkSunlightList);
        }
        
        for (int i = 0; i < entities.size(); i++)
        {
            entities.get(i).tick();
        }
    }
    
    private final Matrix4f mv = new Matrix4f();
    private final float[] skyColor = new float[3];
    
    public void draw(int drawMode, MatrixHandler mh, MatrixHandler mhSun)
    {
        if (!isInitialized)
        {
            throw new IllegalStateException("This world isn't initialized yet.");
        }
        
        if (drawMode != Game.DRAW_ACTUAL && drawMode != Game.DRAW_LIGHTMAP)
        {
            throw new IllegalArgumentException("Invalid draw mode: " + drawMode);
        }
        
        if (drawMode == Game.DRAW_ACTUAL)
        {
            player.getViewMatrix(mv);
            mh.setView(mv);
            mh.updateFrustum();
            
            double sa = getSunAngle();
            
            double[] pos = player.getPos();
            double sunDist = Settings.getZFar() * .99;
            double dx = Math.cos(sa) * sunDist;
            double dz = Math.sin(sa) * sunDist;
            float scl = (float)(sunDist / 32);
            
            mh.setModelMatrix((float)(pos[0] + dx), (float)pos[1], (float)(pos[2] + dz), scl, scl, scl, 0, (float)(- Math.PI/2 - sa), 0);
            
            //glDepthMask(false);
            //glDisable(GL_DEPTH_TEST);
            Shader.sun.use();
            ModelLoader.basicModels.draw(mh, BasicModels.QUAD);
            //glDepthMask(true);
            //glEnable(GL_DEPTH_TEST);
            
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D_ARRAY, TextureLoader.block);
            
            Shader.block.use();
            glUniform1f(Shader.block.getUniformLocation(2), (float)sa);
            
            getSkyColor(skyColor);
            glUniform3f(Shader.block.getUniformLocation(8), skyColor[0], skyColor[1], skyColor[2]);
        }
        else if (drawMode == Game.DRAW_LIGHTMAP)
        {
            Shader.lightmap.use();
        }
        
        ArrayList<Chunk> cList;
        
        if (drawMode == Game.DRAW_ACTUAL)
        {
            cList = chunkList;
        }
        else if (drawMode == Game.DRAW_LIGHTMAP)
        {
            cList = chunkSunlightList;
        }
        
        for (int i = 0; i < chunkList.size(); i++)
        {
            chunkList.get(i).draw(drawMode, mh, mhSun);
        } 
        
        glUseProgram(0);
        
        if (drawMode == Game.DRAW_ACTUAL)
        {
            glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        }
        /*glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);*/
        glBindVertexArray(0);
        
        if (drawMode == Game.DRAW_ACTUAL)
        {
            Block b = player.getTargetBlock();

            if (b != null && b.exists)
            {
                float s = 1 + 1f/128;
                float t = -1f/256;

                mh.setModelMatrix(new Vector3f(b.x + t, b.y + t, b.z + t),
                        new Vector3f(s, s, s), MatrixHandler.vec0);

                ModelLoader.blockSelectBox.draw(mh);
            }
        }
        
        WorldOfCaves.checkGLErrors("World.draw()");
    }
    
    public void destroy()
    {
        try
        {
            WorldIO.saveWorld(this);
        }
        catch (IOException e)
        {
            WorldOfCaves.logError("Unable to save world.", e);
        }
        
        for (int i = 0; i < chunkList.size(); i++)
        {
            chunkList.get(i).destroy();
        }
    }
    
    private void loadChunk(Vector3i[] chunks)
    {
        for (int i = 0; i < chunks.length; i++)
        {
            loadChunk(chunks[i]);
        }
    }
    private Chunk loadChunk(Vector3i chunk)
    {
        return loadChunk(chunk.x, chunk.y, chunk.z);
    }
    private Chunk loadChunk(int chunkX, int chunkY, int chunkZ)
    {
        Chunk c = wg.generate(this, chunkX, chunkY, chunkZ);
        addChunk(c);
        return c;
    }
    
    private Chunk loadNext()
    {
        Vector3i v = getNextChunk();
        return loadChunk(v.x, v.y, v.z);
    }
    
    private Vector3i getNextChunk()
    {
        return getNextChunks(1)[0];
    }
    private Vector3i[] getNextChunks(int amount)
    {
        double[] pos = player.getPos();
        
        double xr = renderDist / Chunk.xSize;
        double yr = renderDist / Chunk.ySize;
        double zr = renderDist / Chunk.zSize * renderDistZFactor;
        
        int x1 = (int)(pos[0] / Chunk.xSize - xr);
        int x2 = (int)(pos[0] / Chunk.xSize + xr);
        int y1 = (int)(pos[1] / Chunk.ySize - yr);
        int y2 = (int)(pos[1] / Chunk.ySize + yr);
        int z1 = (int)(pos[2] / Chunk.zSize - zr);
        int z2 = (int)(pos[2] / Chunk.zSize + zr);
        
        //int cx = 0, cy = 0, cz = 0;
        //float maxPrio = Float.NEGATIVE_INFINITY;
        
        int[] cx = new int[amount];
        int[] cy = new int[amount];
        int[] cz = new int[amount];
        float[] maxPrio = new float[amount];
        int found = 0;
        
        for (int i = 0; i < amount; i++)
        {
            maxPrio[i] = Float.NEGATIVE_INFINITY;
        }
        
        for (int zz = z1; zz <= z2; zz++)
        {
            for (int yy = y1; yy <= y2; yy++)
            {
                for (int xx = x1; xx <= x2; xx++)
                {
                    float prio = getChunkLoadPriority(xx, yy, zz);
                    
                    if (prio > maxPrio[amount-1] && !chunkExists(xx, yy, zz))
                    {
                        for (int i = 0; i < amount; i++)
                        {
                            if (prio > maxPrio[i])
                            {
                                for (int j = found - 1; j >= i+1; j--)
                                {
                                    cx[j] = cx[j-1];
                                    cy[j] = cy[j-1];
                                    cz[j] = cz[j-1];
                                    maxPrio[j] = maxPrio[j-1];
                                }
                                
                                cx[i] = xx;
                                cy[i] = yy;
                                cz[i] = zz;
                                maxPrio[i] = prio;
                                if (found < amount)
                                {
                                    found++;
                                }
                                
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        Vector3i[] res = new Vector3i[found];
        for (int i = 0; i < found; i++)
        {
            res[i] = new Vector3i(cx[i], cy[i], cz[i]);
        }
        
        return res;
    }
    
    public Chunk getFarthestChunk()
    {
        return getFarthestChunk(false);
    }
    private Chunk getFarthestChunk(boolean removeFar)
    {
        Chunk mc = null;
        float minPrio = Float.POSITIVE_INFINITY;
        
        for (int i = 0; i < chunkList.size(); i++)
        {
            Chunk c = chunkList.get(i);
            float prio = getChunkLoadPriority(c);
            
            if (removeFar && prio < minPriority)
            {
                removeChunk(c);
            }
            else if (prio < minPrio)
            {
                minPrio = prio;
                mc = c;
            }
        }
        
        return mc;
    }
    
    private final Vector3f chunkPos = new Vector3f(),
            playerPos = new Vector3f(),
            diff = new Vector3f();
    
    public float getChunkLoadPriority(Vector3i v)
    {
        return getChunkLoadPriority(v.x, v.y, v.z);
    }
    public float getChunkLoadPriority(Chunk c)
    {
        return getChunkLoadPriority(c.x, c.y, c.z);
    }
    public float getChunkLoadPriority(int chunkX, int chunkY, int chunkZ)
    {
        return getChunkLoadPriority(chunkX, chunkY, chunkZ, false);
    }
    private float getChunkLoadPriority(int chunkX, int chunkY, int chunkZ, boolean checkFrustum)
    {
        //float zSp = (float)player.getZSpeed();
        
        if (checkFrustum)
        {
            throw new IllegalArgumentException("checkFrustum cannot be true.");
        }
        
        chunkPos.x = (chunkX + .5f) * Chunk.xSize;
        chunkPos.y = (chunkY + .5f) * Chunk.ySize;
        chunkPos.z = (chunkZ + .5f) * Chunk.zSize * zPrioBias;
        
        playerPos.x = (float)player.getX();
        playerPos.y = (float)player.getY();
        playerPos.z = (float)player.getZ() * zPrioBias;
        
        Vector3f.sub(chunkPos, playerPos, diff);
        
        float dist = diff.lengthSquared();
        
        if (Math.abs(dist) < 16)
        {
            dist = 16;
        }
        
        float prio = 1f / dist;
        
        if (checkFrustum)
        {
            Chunk c = getChunk(chunkX, chunkY, chunkZ);

            if (c != null && c.isInFrustum(game.getMatrixHandler()))
            {
                prio *= 2;
            }
        }
        
        return prio;
    }
    
    public ChunkModel getBlockModel(int x, int y, int z)
    {
        Chunk c = getBlockChunk(x, y, z);
        
        if (c == null)
        {
            return null;
        }
        
        return c.getBlockModel(
                        Util.mod(x, Chunk.xSize),
                        Util.mod(y, Chunk.ySize),
                        Util.mod(z, Chunk.zSize));
    }
    
    public void removeBlock(Block b)
    {
        removeBlock(b, 1);
    }
    public void removeBlock(Block b, float prio)
    {
        Chunk c = getBlockChunk(b.x, b.y, b.z);
        
        if (c == null)
        {
            throw new IllegalArgumentException("Block does not exist:" + b);
        }
        
        c.setBlock(new Block(Block.AIR, b.x, b.y, b.z), prio, true);
    }
    
    public void removeBlock(int x, int y, int z)
    {
        Chunk c = getBlockChunk(x, y, z);
        
        if (c == null)
        {
            throw new IllegalArgumentException("Block does not exist:" + "\nx: " + x + "\ny: " + y + "\nz: " + z);
        }
        
        c.setBlock(new Block(Block.AIR, x, y, z));
    }
    
    public void setBlock(Block b)
    {
        setBlock(b, 1);
    }
    public void setBlock(Block b, float prio)
    {
        setBlock(b, prio, false);
    }
    public void setBlock(Block b, float prio, boolean invertPrio)
    {
        getBlockChunk(b).setBlock(b, prio, invertPrio);
    }
    
    public Chunk getChunk(int cx, int cy, int cz)
    {
        return chunkMap.get(new Vector3i(cx, cy, cz));
    }
    
    public boolean chunkExists(int cx, int cy, int cz)
    {
        return chunkExists(new Vector3i(cx, cy, cz));
    }
    public boolean chunkExists(Vector3i pos)
    {
        return chunkMap.containsKey(pos);
    }
    
    public Chunk getBlockChunk(int x, int y, int z)
    {
        return getChunk(
                (int)Math.floor((double)x / Chunk.xSize),
                (int)Math.floor((double)y / Chunk.ySize),
                (int)Math.floor((double)z / Chunk.zSize));
    }
    
    public Chunk getBlockChunk(Block b)
    {
        return getBlockChunk(b.x, b.y, b.z);
    }
    
    public byte getBlockId(int x, int y, int z)
    {
        Chunk c = getBlockChunk(x, y, z);
        
        if (c == null)
        {
            return Block.NONE;
        }
        
        return c.getBlockId(
                        Util.mod(x, Chunk.xSize),
                        Util.mod(y, Chunk.ySize),
                        Util.mod(z, Chunk.zSize));
    }
    
    public Block getBlock(int x, int y, int z)
    {
        Chunk c = getBlockChunk(x, y, z);
        
        if (c == null)
        {
            return new Block(Block.NONE);
        }
        
        return c.getBlock(
                        Util.mod(x, Chunk.xSize),
                        Util.mod(y, Chunk.ySize),
                        Util.mod(z, Chunk.zSize));
    }
    
    public Player getPlayer()
    {
        return player;
    }
    
    @Override
    public void encode(TagCompound data)
    {
        data.add(new TagString("name", name));
        data.add(new TagLong("currentTick", currentTick));
        data.add(new TagInt("fracTick", fracTick));
        
        data.add(Encoder.encode("worldGenerator", wg));
        data.add(Encoder.encode("player", player));
    }
    
    public static World decode(TagCompound data, Game game) throws TagFormatException
    {
        String name = data.findString("name", "Unknown");
        WorldGenerator wg = WorldGenerator.decode(data.findCompoundTag("worldGenerator"));
        
        World world = new World(game, name, wg, null, false);
        world.player = (Player)Entity.decode(data.findCompoundTag("player"), world);
        world.currentTick = data.findLong("currentTick", 0);
        //world.currentTick = 9000;
        world.fracTick = data.findInt("fracTick", 0);
        
        world.init();
        
        return world;
    }
}