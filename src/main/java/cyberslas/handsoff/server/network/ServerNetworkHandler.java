package cyberslas.handsoff.server.network;

import cyberslas.handsoff.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.Collection;

public class ServerNetworkHandler {
    public static <T> void sendToPlayer(ServerPlayer player, T packet) {
        NetworkHandler.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static <T> void sendToAllPlayers(T packet) {
        NetworkHandler.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static <T> void sendToPlayersInRange(GlobalPos pos, int range, T packet) {
        BlockPos blockPos = pos.pos();
        NetworkHandler.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(blockPos.getX(), blockPos.getY(), blockPos.getZ(), range, pos.dimension())), packet);
    }

    public static <T> void sendToPlayers(Collection<ServerPlayer> playerCollection, T packet) {
        for(ServerPlayer player : playerCollection) {
            NetworkHandler.send(PacketDistributor.PLAYER.with(() -> player), packet);
        }
    }
}
