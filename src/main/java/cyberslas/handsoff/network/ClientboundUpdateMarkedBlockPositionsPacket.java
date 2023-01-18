package cyberslas.handsoff.network;

import cyberslas.handsoff.client.network.ClientPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public record ClientboundUpdateMarkedBlockPositionsPacket(Set<BlockPos> unlockedPositions, Set<BlockPos> lockedPositions) {
    public static void encode(ClientboundUpdateMarkedBlockPositionsPacket packet, FriendlyByteBuf buffer) {
        buffer.writeCollection(packet.unlockedPositions(), FriendlyByteBuf::writeBlockPos);
        buffer.writeCollection(packet.lockedPositions(), FriendlyByteBuf::writeBlockPos);
    }

    public static ClientboundUpdateMarkedBlockPositionsPacket decode(FriendlyByteBuf buffer) {
        return new ClientboundUpdateMarkedBlockPositionsPacket(buffer.readCollection(HashSet::new, FriendlyByteBuf::readBlockPos), buffer.readCollection(HashSet::new, FriendlyByteBuf::readBlockPos));
    }

    public static void handle(ClientboundUpdateMarkedBlockPositionsPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleMarkerUpdate(packet, context)));
        context.get().setPacketHandled(true);
    }
}
