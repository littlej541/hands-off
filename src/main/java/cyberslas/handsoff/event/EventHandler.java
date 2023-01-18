package cyberslas.handsoff.event;

import cyberslas.handsoff.server.MarkedBlockMap;
import cyberslas.handsoff.util.Constants;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public class EventHandler {
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        MarkedBlockMap.init(event.getServer());
    }
}
