package cyberslas.handsoff.network;

import cyberslas.handsoff.util.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NetworkHandler {
    private static NetworkHandler INSTANCE;
    private final String PROTOCOL_VERSION = "1";
    private final SimpleChannel simpleChannel;
    private int id = 0;

    public static void init() {
        INSTANCE = new NetworkHandler();

        registerPacket(ClientboundUpdateMarkedBlockPositionsPacket.class, ClientboundUpdateMarkedBlockPositionsPacket::encode, ClientboundUpdateMarkedBlockPositionsPacket::decode, ClientboundUpdateMarkedBlockPositionsPacket::handle, NetworkDirection.PLAY_TO_CLIENT);
        registerPacket(ServerboundRequestMarkedBlockPositionsPacket.class, ServerboundRequestMarkedBlockPositionsPacket::encode, ServerboundRequestMarkedBlockPositionsPacket::decode, ServerboundRequestMarkedBlockPositionsPacket::handle, NetworkDirection.PLAY_TO_SERVER);
        registerPacket(ClientboundMarkResultPacket.class, ClientboundMarkResultPacket::encode, ClientboundMarkResultPacket::decode, ClientboundMarkResultPacket::handle, NetworkDirection.PLAY_TO_CLIENT);
    }

    private NetworkHandler() {
        this.simpleChannel = NetworkRegistry.newSimpleChannel(new ResourceLocation(Constants.MODID, "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
    }

    public static <T> void send(PacketDistributor.PacketTarget target, T packet) {
        INSTANCE.simpleChannel.send(target, packet);
    }

    public static <T> void registerPacket(Class<T> clazz, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> handler, NetworkDirection networkDirection) {
        INSTANCE.simpleChannel.registerMessage(INSTANCE.id++, clazz, encoder, decoder, handler, Optional.of(networkDirection));
    }


}
