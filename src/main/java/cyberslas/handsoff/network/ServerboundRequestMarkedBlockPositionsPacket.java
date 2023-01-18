package cyberslas.handsoff.network;

import cyberslas.handsoff.server.network.ServerPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ServerboundRequestMarkedBlockPositionsPacket() {
    public static void encode(ServerboundRequestMarkedBlockPositionsPacket packet, FriendlyByteBuf buffer) {

    }

    public static ServerboundRequestMarkedBlockPositionsPacket decode(FriendlyByteBuf buffer) {
        return new ServerboundRequestMarkedBlockPositionsPacket();
    }

    public static void handle(ServerboundRequestMarkedBlockPositionsPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> ServerPacketHandler.handleBlockPositionRequest(packet, context));
        context.get().setPacketHandled(true);
    }
}
