package cyberslas.handsoff.server.util;

import cyberslas.handsoff.config.Config;
import cyberslas.handsoff.network.ClientboundMarkResultPacket;
import cyberslas.handsoff.network.ClientboundUpdateMarkedBlockPositionsPacket;
import cyberslas.handsoff.server.MarkedBlockManager;
import cyberslas.handsoff.server.network.ServerNetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ServerHelper {
    public static Set<ServerPlayer> getPlayersInRange(MinecraftServer server, GlobalPos pos, int range) {
        return server.getPlayerList().getPlayers().stream()
                .filter(player -> player.level.dimension().equals(pos.dimension()) && Math.sqrt(pos.pos().distToCenterSqr(player.position())) <= range)
                .collect(Collectors.toSet());
    }

    public static void clearPoiAndMemory(LivingEntity entity, MemoryModuleType<GlobalPos> memoryModuleType) {
        Brain<?> brain = entity.getBrain();
        Optional<GlobalPos> globalPosOptional = brain.getMemory(memoryModuleType);
        if (globalPosOptional.isPresent()) {
            GlobalPos pos = globalPosOptional.get();
            ServerLevel level = entity.getServer().getLevel(pos.dimension());
            PoiManager poimanager = level.getPoiManager();

            if (poimanager.exists(pos.pos(), (poiType) -> true)) {
                poimanager.release(pos.pos());
            }

            brain.eraseMemory(memoryModuleType);

            if (memoryModuleType == MemoryModuleType.HOME && entity.isSleeping() && entity.getSleepingPos().get().equals(pos.pos())) {
                entity.stopSleeping();
            }
        }
    }

    public static boolean removeFromBlockOwnershipMapIfExists(PoiManager poiManager, GlobalPos pos, Predicate<Holder<PoiType>> poiTypePredicate) {
        boolean exists = poiManager.exists(pos.pos(), poiTypePredicate);
        if (!exists) {
            MarkedBlockManager.remove(pos);
        }

        return exists;
    }

    public static class Network {
        public static void sendMarkedBlocksUpdate(ServerPlayer player, int range) {
            UUID uuid = player.getUUID();
            Set<BlockPos> unlockedPositions = MarkedBlockManager.getPosMarkedByUUID(uuid);
            Set<BlockPos> lockedPositions = MarkedBlockManager.getPosMarkedByOtherUUID(uuid);

            Set<BlockPos> inRangeSet = player.getLevel().getPoiManager()
                    .getInRange(MarkedBlockManager::testPoi, player.blockPosition(), range, PoiManager.Occupancy.ANY)
                    .map(PoiRecord::getPos)
                    .collect(Collectors.toSet());

            unlockedPositions.retainAll(inRangeSet);
            lockedPositions.retainAll(inRangeSet);

            if (!Config.COMMON.lockToPlayer.get()) {
                unlockedPositions.addAll(lockedPositions);
                lockedPositions.clear();
            }

            ServerNetworkHandler.sendToPlayer(player, new ClientboundUpdateMarkedBlockPositionsPacket(unlockedPositions, lockedPositions));
        }

        public static void sendMarkResult(ServerPlayer player, BlockPos pos, ClientboundMarkResultPacket.Result result) {
            ServerNetworkHandler.sendToPlayer(player, new ClientboundMarkResultPacket(pos, result));
        }
    }
}
