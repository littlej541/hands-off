package cyberslas.handsoff.network;

import cyberslas.handsoff.client.network.ClientPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClientboundMarkResultPacket(BlockPos pos, Result result) {
    public static void encode(ClientboundMarkResultPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos());
        buffer.writeEnum(packet.result());
    }

    public static ClientboundMarkResultPacket decode(FriendlyByteBuf buffer) {
        return new ClientboundMarkResultPacket(buffer.readBlockPos(), buffer.readEnum(Result.class));
    }

    public static void handle(ClientboundMarkResultPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleMarkResult(packet, context)));
        context.get().setPacketHandled(true);
    }

    public enum Result {
        MARKED,
        UNMARKED,
        INVALID,
        MARKED_OTHER_PLAYER
    }
}
