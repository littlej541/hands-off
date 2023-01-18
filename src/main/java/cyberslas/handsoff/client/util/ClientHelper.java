package cyberslas.handsoff.client.util;

import cyberslas.handsoff.client.network.ClientNetworkHandler;
import cyberslas.handsoff.network.ServerboundRequestMarkedBlockPositionsPacket;

public class ClientHelper {
    public static class Network {
        public static void requestMarkedBlocks() {
            ClientNetworkHandler.sendToServer(new ServerboundRequestMarkedBlockPositionsPacket());
        }
    }
}
