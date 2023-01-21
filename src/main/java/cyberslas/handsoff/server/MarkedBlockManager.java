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
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MarkedBlockManager {
    private static MarkedBlockManager INSTANCE;
    private static MinecraftServer SERVER;

    private final Map<ChunkId, ChunkData> chunkMap = new HashMap<>();
    private final Multimap<UUID, GlobalPos> uuidMarkedMap = HashMultimap.create();
    private final Set<Predicate<Holder<PoiType>>> POI_PREDICATES = new HashSet<>();
    private final Set<Predicate<Holder<PoiType>>> EXTRA_POI_PREDICATES = new HashSet<>();
    private final Set<Block> POI_BLOCKS = new HashSet<>();
    private final Predicate<Holder<PoiType>> POI_TEST = poiType -> POI_PREDICATES.stream().anyMatch(predicate -> predicate.test(poiType)) ||
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
        LevelChunk chunk = getChunkFromGlobalPos(pos);
        ChunkData data = INSTANCE.getOrCreateChunkData(chunk);

        return data.contains(pos);
    }

    public static UUID get(GlobalPos pos) {
        LevelChunk chunk = getChunkFromGlobalPos(pos);
        ChunkData data = INSTANCE.getOrCreateChunkData(chunk);

        return data.get(pos);
    }

    public static Set<BlockPos> getPosMarkedByUUID(UUID uuid) {
        return INSTANCE.uuidMarkedMap.get(uuid).stream().map(GlobalPos::pos).collect(Collectors.toSet());
    }

    public static Set<BlockPos> getPosMarkedByOtherUUID(UUID uuid) {
        Multimap<UUID, GlobalPos> removedUUID = HashMultimap.create(INSTANCE.uuidMarkedMap);
        removedUUID.removeAll(uuid);
        return removedUUID.values().stream().map(GlobalPos::pos).collect(Collectors.toSet());
    }

    public static boolean testPoi(Holder<PoiType> toTest) {
        return INSTANCE.POI_TEST.test(toTest);
    }

    private static LevelChunk getChunkFromGlobalPos(GlobalPos pos) {
        return (LevelChunk)getLevelFromGlobalPos(pos).getChunk(pos.pos());
    }

    private static ServerLevel getLevelFromGlobalPos(GlobalPos pos) {
        return SERVER.getLevel(pos.dimension());
    }

    private static ChunkId getUniqueChunkId(LevelChunk chunk) {
        return new ChunkId(chunk.getLevel().dimension(), chunk.getPos());
    }

    private static Predicate<Holder<PoiType>> makePredicateWithLambda(Predicate<Holder<PoiType>> lambdaFunction) {
        return lambdaFunction;
    }

    private MarkedBlockManager() {
        this.POI_PREDICATES.addAll(ForgeRegistries.POI_TYPES.getValues().stream()
                .filter(poiType -> {
                    Holder<PoiType> holder = ForgeRegistries.POI_TYPES.getHolder(poiType).get();
                    return VillagerProfession.ALL_ACQUIRABLE_JOBS.test(holder) ||
                            holder.is(PoiTypes.HOME);
                })
                .map(poiType -> {
                    ResourceKey<PoiType> holder = ForgeRegistries.POI_TYPES.getResourceKey(poiType).get();
                    return makePredicateWithLambda(l -> l.is(holder));
                })
                .collect(Collectors.toSet()));
        this.EXTRA_POI_PREDICATES.addAll(Config.COMMON.extraPoiTypes.get().stream()
                .map(mapping -> {
                    String[] splitPair = mapping.split(":", 2);

                    if (splitPair.length < 2) {
                        return null;
                    }

                    Optional<ResourceKey<PoiType>> optionalHolder = ForgeRegistries.POI_TYPES.getResourceKey(ForgeRegistries.POI_TYPES.getValue(new ResourceLocation(splitPair[0], splitPair[1])));
                    if (optionalHolder.isPresent()) {
                        return makePredicateWithLambda(l -> l.is(optionalHolder.get()));
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        this.POI_BLOCKS.addAll(ForgeRegistries.POI_TYPES.getValues().stream()
                .filter(poiType -> this.POI_TEST.test(ForgeRegistries.POI_TYPES.getHolder(poiType).get()))
                .flatMap(item -> {
                    Set<BlockState> blockStates = item.matchingStates();

                    return blockStates.stream()
                            .map(blockState -> {
                                Block block = blockState.getBlock();
                                this.BLOCK_POI_MAPPING.put(block, item);

                                return block;
                            });
                })
                .filter(Objects::nonNull).collect(Collectors.toSet()));
    }

    private ChunkData getOrCreateChunkData(LevelChunk chunk) {
        ChunkId id = getUniqueChunkId(chunk);
        if (!this.chunkMap.containsKey(id)) {
            this.chunkMap.put(id, new ChunkData());
        }

        return this.chunkMap.get(id);
    }

    private boolean putDirect(GlobalPos pos, UUID uuid) {
        LevelChunk chunk = getChunkFromGlobalPos(pos);
        BlockState blockState = chunk.getBlockState(pos.pos());
        Block block = blockState.getBlock();

        if (this.POI_BLOCKS.stream().anyMatch(block::equals)) {
            ChunkData data = this.getOrCreateChunkData(chunk);
            data.put(pos, uuid, blockState);
            this.uuidMarkedMap.put(uuid, pos);

            return true;
        }

        return false;
    }

    private boolean removeDirect(GlobalPos pos) {
        LevelChunk chunk = getChunkFromGlobalPos(pos);
        ChunkData data = this.getOrCreateChunkData(chunk);
        Pair<UUID, BlockState> pair = data.remove(pos);
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
        getChunkFromGlobalPos(pos).setUnsaved(true);
    }

    private void clearBadEntries(ChunkData data) {
        Set<GlobalPos> markedForRemoval = new HashSet<>();
        for(Map.Entry<GlobalPos, Pair<UUID, BlockState>> entry : data.markedBlockMap.entrySet()) {
            BlockPos blockPos = entry.getKey().pos();
            BlockState blockState = entry.getValue().getSecond();
            Block block = blockState.getBlock();
            ResourceKey<PoiType> holder = ForgeRegistries.POI_TYPES.getResourceKey(INSTANCE.BLOCK_POI_MAPPING.get(block)).get();

            if (!SERVER.getLevel(entry.getKey().dimension()).getPoiManager().exists(blockPos, makePredicateWithLambda(x -> x.is(holder)))) {
                markedForRemoval.add(entry.getKey());
            }
        }

        for(GlobalPos pos : markedForRemoval) {
            this.removeDirect(pos);
        }
    }

    public static CompoundTag saveChunkData(LevelAccessor level, ChunkAccess chunkAccess, CompoundTag compoundTag) {
        if (chunkAccess instanceof LevelChunk chunk) {
            ChunkData data = INSTANCE.getOrCreateChunkData(chunk);
            if (data.markForDelete) {
                INSTANCE.chunkMap.remove(getUniqueChunkId(chunk)).markedBlockMap.forEach((key, value) -> INSTANCE.uuidMarkedMap.remove(value.getFirst(), key));
            }
            INSTANCE.clearBadEntries(data);

            ListTag listTag = new ListTag();
            for (Map.Entry<GlobalPos, Pair<UUID, BlockState>> entry : data.markedBlockMap.entrySet()) {
                CompoundTag newTag = new CompoundTag();

                DataResult<Tag> dataResult = GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, entry.getKey());
                newTag.put("globalPos", dataResult.resultOrPartial(error -> HandsOff.logger.error(error)).get());
                newTag.putUUID("uuid", entry.getValue().getFirst());
                newTag.put("blockState", NbtUtils.writeBlockState(entry.getValue().getSecond()));
                listTag.add(newTag);
            }

            compoundTag.put("MarkedBlockData", listTag);
        }

        return compoundTag;
    }

    public static void loadChunkData(LevelAccessor level, ChunkAccess chunkAccess, CompoundTag compoundTag) {
        if (chunkAccess instanceof LevelChunk chunk) {
            List<Pair<GlobalPos, Pair<UUID, BlockState>>> list = compoundTag.getList("MarkedBlockData", Tag.TAG_COMPOUND).stream()
                    .map(tag -> Pair.of(GlobalPos.CODEC.parse(NbtOps.INSTANCE, ((CompoundTag) tag).get("globalPos")).resultOrPartial(error -> HandsOff.logger.error(error)).get(), Pair.of(((CompoundTag) tag).getUUID("uuid"), NbtUtils.readBlockState(((CompoundTag) tag).getCompound("blockState")))))
                    .toList();

            ChunkData data = INSTANCE.getOrCreateChunkData(chunk);

            list.forEach(entry -> {
                data.put(entry.getFirst(), entry.getSecond().getFirst(), entry.getSecond().getSecond());
                INSTANCE.uuidMarkedMap.put(entry.getSecond().getFirst(), entry.getFirst());
            });
        }
    }

    public static void unloadChunk(LevelAccessor level, ChunkAccess chunkAccess) {
        if (chunkAccess instanceof LevelChunk chunk) {
            ChunkId id = getUniqueChunkId(chunk);
            INSTANCE.chunkMap.get(id).markForDelete = true;
        }
    }

    private static class ChunkData {
        private final Map<GlobalPos, Pair<UUID, BlockState>> markedBlockMap;
        private boolean markForDelete = false;

        private ChunkData() {
            this.markedBlockMap = new HashMap<>();
        }

        private Pair<UUID, BlockState> put(GlobalPos pos, UUID uuid, BlockState blockState) {
            return this.markedBlockMap.put(pos, Pair.of(uuid, blockState));
        }

        private Pair<UUID, BlockState> remove(GlobalPos pos) {
            return this.markedBlockMap.remove(pos);
        }

        private UUID get(GlobalPos pos) {
            return this.markedBlockMap.get(pos).getFirst();
        }

        private boolean contains(GlobalPos pos) {
            return this.markedBlockMap.containsKey(pos);
        }
    }

    private record ChunkId(ResourceKey<Level> dimension, ChunkPos pos) {
        @Override
        public boolean equals(Object other) {
            if (other instanceof ChunkId otherId) {
                return this.dimension.equals(otherId.dimension()) && this.pos.equals(otherId.pos());
            }

            return false;
        }
    }
}
