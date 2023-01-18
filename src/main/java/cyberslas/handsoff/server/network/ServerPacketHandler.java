package cyberslas.handsoff.server.network;

import cyberslas.handsoff.network.ServerboundRequestMarkedBlockPositionsPacket;
import cyberslas.handsoff.server.util.ServerHelper;
import cyberslas.handsoff.util.Helper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerPacketHandler {
    public static void handleBlockPositionRequest(ServerboundRequestMarkedBlockPositionsPacket packet, Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        int range = Helper.getMaxBlockRenderRange(player.server.getPlayerList().getViewDistance());
        ServerHelper.Network.sendMarkedBlocksUpdate(player, range);
    }
}
