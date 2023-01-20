package cyberslas.handsoff.server;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import cyberslas.handsoff.HandsOff;
import cyberslas.handsoff.config.Config;
import cyberslas.handsoff.server.util.ServerHelper;
import cyberslas.handsoff.util.Helper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MarkedBlockManager {
    private static MarkedBlockManager INSTANCE;
    private static MinecraftServer SERVER;

    private final Map<ChunkAccess, ChunkData> chunkMap = new HashMap<>();
    private final Multimap<UUID, GlobalPos> uuidMarkedMap = HashMultimap.create();
    private final Set<Predicate<PoiType>> POI_PREDICATES = new HashSet<>();
    private final Set<Predicate<PoiType>> EXTRA_POI_PREDICATES = new HashSet<>();
    private final Set<Block> POI_BLOCKS = new HashSet<>();
    private final Predicate<PoiType> POI_TEST = poiType -> POI_PREDICATES.stream().anyMatch(predicate -> predicate.test(poiType)) ||
            EXTRA_POI_PREDICATES.stream().anyMatch(predicate -> predicate.test(poiType));
    private final Map<Block, PoiType> BLOCK_POI_MAPPING = new HashMap<>();

    public static void init(MinecraftServer server) {
        INSTANCE = new MarkedBlockManager();
        SERVER = server;
    }

    public static boolean put(GlobalPos pos, UUID uuid) {
        boolean returnVal = INSTANCE.putDirect(pos, uuid);
        if (returnVal) {
            INSTANCE.setDirty(pos);
        }

        return returnVal;
    }

    public static boolean remove(GlobalPos pos) {
        boolean returnVal = INSTANCE.removeDirect(pos);
        if (returnVal) {
            INSTANCE.setDirty(pos);
        }

        return returnVal;
    }

    public static boolean contains(GlobalPos pos) {
        ChunkAccess chunk = getChunk(pos);
        return INSTANCE.chunkMap.get(chunk).contains(pos);
    }

    public static UUID get(GlobalPos pos) {
        ChunkAccess chunk = getChunk(pos);
        return INSTANCE.chunkMap.get(chunk).get(pos);
    }

    public static Set<BlockPos> getPosMarkedByUUID(UUID uuid) {
        return INSTANCE.uuidMarkedMap.get(uuid).stream().map(GlobalPos::pos).collect(Collectors.toSet());
    }

    public static Set<BlockPos> getPosMarkedByOtherUUID(UUID uuid) {
        Multimap<UUID, GlobalPos> removedUUID = HashMultimap.create(INSTANCE.uuidMarkedMap);
        removedUUID.removeAll(uuid);
        return removedUUID.values().stream().map(GlobalPos::pos).collect(Collectors.toSet());
    }

    public static boolean testPoi(PoiType toTest) {
        return INSTANCE.POI_TEST.test(toTest);
    }

    private static ChunkAccess getChunk(GlobalPos pos) {
        return SERVER.getLevel(pos.dimension()).getChunk(pos.pos());
    }

    private MarkedBlockManager() {
        this.POI_PREDICATES.addAll(ForgeRegistries.POI_TYPES.getValues().stream()
                .filter(poiType -> PoiType.ALL_JOBS.test(poiType) || PoiType.HOME.getPredicate().test(poiType))
                .map(PoiType::getPredicate)
                .collect(Collectors.toSet()));
        this.EXTRA_POI_PREDICATES.addAll(Config.COMMON.extraPoiTypes.get().stream()
                .map(mapping -> {
                    String[] splitPair = mapping.split(":", 2);

                    if (splitPair.length < 2) {
                        return null;
                    }

                    return ForgeRegistries.POI_TYPES.getValue(new ResourceLocation(splitPair[0], splitPair[1])).getPredicate();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        this.POI_BLOCKS.addAll(ForgeRegistries.POI_TYPES.getValues().stream()
                .filter(this.POI_TEST)
                .flatMap(item -> {
                    Set<BlockState> blockStates = item.getBlockStates();

                    return blockStates.stream()
                            .map(blockState -> {
                                Block block = blockState.getBlock();
                                this.BLOCK_POI_MAPPING.put(block, item);

                                return block;
                            });
                })
                .filter(Objects::nonNull).collect(Collectors.toSet()));
    }

    private boolean putDirect(GlobalPos pos, UUID uuid) {
        ChunkAccess chunk = getChunk(pos);
        BlockState blockState = chunk.getBlockState(pos.pos());
        Block block = blockState.getBlock();

        if (this.POI_BLOCKS.stream().anyMatch(block::equals)) {
            chunkMap.get(chunk).put(pos, uuid, blockState);
            this.uuidMarkedMap.put(uuid, pos);

            return true;
        }

        return false;
    }

    private boolean removeDirect(GlobalPos pos) {
        ChunkAccess chunk = getChunk(pos);
        Pair<UUID, BlockState> pair = this.chunkMap.get(chunk).remove(pos);
        if (pair != null) {
            this.uuidMarkedMap.remove(pair.getFirst(), pos);
            return true;
        }

        return false;
    }

    private void setDirty(GlobalPos pos) {
        int range = Helper.getMaxBlockRenderRange(SERVER.getPlayerList().getViewDistance());
        Set<ServerPlayer> players = ServerHelper.getPlayersInRange(SERVER, pos, range);
        for(ServerPlayer player : players) {
            ServerHelper.Network.sendMarkedBlocksUpdate(player, range);
        }
    }

    public static CompoundTag saveChunk(LevelAccessor level, ChunkAccess chunk, CompoundTag compoundTag) {
        ChunkData data = INSTANCE.chunkMap.get(chunk);
        data.clearBadEntries();

        ListTag listTag = new ListTag();
        for(Map.Entry<GlobalPos, Pair<UUID, BlockState>> entry : data.markedBlockMap.entrySet()) {
            CompoundTag newTag = new CompoundTag();

            DataResult<Tag> dataResult = GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, entry.getKey());
            newTag.put("globalPos", dataResult.resultOrPartial(error -> HandsOff.logger.error(error)).get());
            newTag.putUUID("uuid", entry.getValue().getFirst());
            newTag.put("blockState", NbtUtils.writeBlockState(entry.getValue().getSecond()));
            listTag.add(newTag);
        }

        compoundTag.put("MarkedBlockData", listTag);
        return compoundTag;
    }

    public static void loadChunk(LevelAccessor level, ChunkAccess chunk, CompoundTag compoundTag) {
        List<Pair<GlobalPos, Pair<UUID, BlockState>>> list =  compoundTag.getList("MarkedBlockData", Tag.TAG_COMPOUND).stream()
                .map(tag -> Pair.of(GlobalPos.CODEC.parse(NbtOps.INSTANCE, ((CompoundTag)tag).get("globalPos")).resultOrPartial(error -> HandsOff.logger.error(error)).get(), Pair.of(((CompoundTag)tag).getUUID("uuid"), NbtUtils.readBlockState(((CompoundTag)tag).getCompound("blockState")))))
                .toList();

        ChunkData data = new ChunkData(((ServerLevel)level).getPoiManager());
        list.forEach(entry -> data.put(entry.getFirst(), entry.getSecond().getFirst(), entry.getSecond().getSecond()));
        INSTANCE.chunkMap.put(chunk, data);
    }

    public static void unloadChunk(ChunkAccess chunk) {
        INSTANCE.chunkMap.remove(chunk);
    }

    private static class ChunkData {
        private final PoiManager poiManager;
        private final Map<GlobalPos, Pair<UUID, BlockState>> markedBlockMap;

        private ChunkData(PoiManager poiManager) {
            this.markedBlockMap = new HashMap<>();
            this.poiManager = poiManager;
        }

        private Pair<UUID, BlockState> put(GlobalPos pos, UUID uuid, BlockState blockState) {
            return this.markedBlockMap.put(pos, Pair.of(uuid, blockState));
        }

        private Pair<UUID, BlockState> remove(GlobalPos pos) {
            return this.markedBlockMap.remove(pos);
        }

        public UUID get(GlobalPos pos) {
            return this.markedBlockMap.get(pos).getFirst();
        }

        private boolean contains(GlobalPos pos) {
            return this.markedBlockMap.containsKey(pos);
        }

        private void clearBadEntries() {
            Set<GlobalPos> markedForRemoval = new HashSet<>();
            for(Map.Entry<GlobalPos, Pair<UUID, BlockState>> entry : this.markedBlockMap.entrySet()) {
                BlockPos blockPos = entry.getKey().pos();
                BlockState blockState = entry.getValue().getSecond();
                Block block = blockState.getBlock();

                if (!this.poiManager.exists(blockPos, INSTANCE.BLOCK_POI_MAPPING.get(block).getPredicate())) {
                    markedForRemoval.add(entry.getKey());
                }
            }

            for(GlobalPos pos : markedForRemoval) {
                this.remove(pos);
            }
        }
    }
}
