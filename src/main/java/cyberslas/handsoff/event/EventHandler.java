package cyberslas.handsoff.event;

import cyberslas.handsoff.server.MarkedBlockManager;
import cyberslas.handsoff.util.Constants;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public class EventHandler {
    @SubscribeEvent
    public static void onServerStarting(ServerAboutToStartEvent event) {
        MarkedBlockManager.init(event.getServer());
    }

    @SubscribeEvent
    public static void onChunkLoadData(ChunkDataEvent.Load event) {
        MarkedBlockManager.loadChunkData(event.getWorld(), event.getChunk(), event.getData());
    }

    @SubscribeEvent
    public static void onChunkSaveData(ChunkDataEvent.Save event) {
        MarkedBlockManager.saveChunkData(event.getWorld(), event.getChunk(), event.getData());
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (!event.getWorld().isClientSide()){
            MarkedBlockManager.unloadChunk(event.getWorld(), event.getChunk());
        }
    }
}
