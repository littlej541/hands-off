package cyberslas.handsoff.client.network;

import cyberslas.handsoff.network.NetworkHandler;
import net.minecraftforge.network.PacketDistributor;

public class ClientNetworkHandler {
    public static <T> void sendToServer(T packet) {
        NetworkHandler.send(PacketDistributor.SERVER.noArg(), packet);
    }
}
