package cyberslas.handsoff.client.event;

import cyberslas.handsoff.client.render.DrawMarkedBlockHighlight;
import cyberslas.handsoff.config.Config;
import cyberslas.handsoff.util.Constants;
import cyberslas.handsoff.client.util.ClientHelper;
import cyberslas.handsoff.util.Helper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public class ClientEventHandler {
    @SubscribeEvent
    public static void onEnterSection(EntityEvent.EnteringSection event) {
        if (Config.CLIENT.showOutlines.get() && event.getEntity() instanceof Player player && Helper.playerHoldingBlockMarker(player)) {
            ClientHelper.Network.requestMarkedBlocks();
        }
    }

    @SubscribeEvent
    public static void onJoinWorld(ClientPlayerNetworkEvent.LoggingIn event) {
        DrawMarkedBlockHighlight.init(Minecraft.getInstance());
        ClientHelper.Network.requestMarkedBlocks();
    }

    @SubscribeEvent
    public static void onRespawn(ClientPlayerNetworkEvent.Clone event) {
        ClientHelper.Network.requestMarkedBlocks();
    }

    @SubscribeEvent
    public static void renderOutline(RenderLevelStageEvent event) {
        if (event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS)) {
            DrawMarkedBlockHighlight.render(event.getLevelRenderer(), event.getCamera(), event.getPoseStack());
        }
    }

    private static boolean doTickUpdate = true;
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side.isClient() && Config.CLIENT.showOutlines.get()) {
            if (Helper.playerHoldingBlockMarker(event.player)) {
                if (doTickUpdate) {
                    ClientHelper.Network.requestMarkedBlocks();
                    doTickUpdate = false;
                }
            } else {
                doTickUpdate = true;
            }
        }
    }
}
