package cyberslas.handsoff.client.network;

import cyberslas.handsoff.client.render.DrawMarkedBlockHighlight;
import cyberslas.handsoff.config.Config;
import cyberslas.handsoff.network.ClientboundMarkResultPacket;
import cyberslas.handsoff.network.ClientboundUpdateMarkedBlockPositionsPacket;
import cyberslas.handsoff.util.Lang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientPacketHandler {
    public static void handleMarkerUpdate(ClientboundUpdateMarkedBlockPositionsPacket packet, Supplier<NetworkEvent.Context> context) {
        DrawMarkedBlockHighlight.setBlockPositions(packet.unlockedPositions(), packet.lockedPositions());
    }

    public static void handleMarkResult(ClientboundMarkResultPacket packet, Supplier<NetworkEvent.Context> context) {
        if (Config.CLIENT.showMessages.get()) {
            Minecraft mc = Minecraft.getInstance();
            ChatComponent chat = mc.gui.getChat();
            BlockPos pos = packet.pos();
            Block block = mc.level.getBlockState(pos).getBlock();
            switch (packet.result()) {
                case MARKED -> chat.addMessage(Component.translatable(Lang.BLOCK_MARKED.getKey(), block.getName(), pos.toShortString()));
                case UNMARKED -> chat.addMessage(Component.translatable(Lang.BLOCK_UNMARKED.getKey(), block.getName(), pos.toShortString()));
                case INVALID -> chat.addMessage(Component.translatable(Lang.BLOCK_INVALID.getKey(), block.getName(), pos.toShortString()));
                case MARKED_OTHER_PLAYER -> chat.addMessage(Component.translatable(Lang.BLOCK_MARKED_OTHER_PLAYER.getKey(), block.getName(), pos.toShortString()));
            }
        }
    }
}
