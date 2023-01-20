package cyberslas.handsoff.event;

import cyberslas.handsoff.server.MarkedBlockManager;
import cyberslas.handsoff.util.Constants;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public class EventHandler {
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        MarkedBlockManager.init(event.getServer());
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkDataEvent.Load event) {
        MarkedBlockManager.loadChunk(event.getWorld(), event.getChunk(), event.getData());
    }

    @SubscribeEvent
    public static void onChunkSave(ChunkDataEvent.Save event) {
        MarkedBlockManager.saveChunk(event.getWorld(), event.getChunk(), event.getData());
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        MarkedBlockManager.unloadChunk(event.getWorld(), event.getChunk());
    }
}
