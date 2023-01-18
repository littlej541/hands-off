package cyberslas.handsoff.server;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import cyberslas.handsoff.HandsOff;
import cyberslas.handsoff.config.Config;
import cyberslas.handsoff.server.util.ServerHelper;
import cyberslas.handsoff.util.Constants;
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
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
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

public class MarkedBlockMap {
    private static MarkedBlockMap INSTANCE;

    private final Map<GlobalPos, Pair<UUID, BlockState>> markedMap = new HashMap<>();
    private final Multimap<UUID, GlobalPos> uuidMarkedMap = HashMultimap.create();
    private final Set<Predicate<PoiType>> POI_PREDICATES = new HashSet<>();
    private final Set<Predicate<PoiType>> EXTRA_POI_PREDICATES = new HashSet<>();
    private final Set<Block> POI_BLOCKS = new HashSet<>();
    private final Predicate<PoiType> POI_TEST = poiType -> POI_PREDICATES.stream().anyMatch(predicate -> predicate.test(poiType)) ||
            EXTRA_POI_PREDICATES.stream().anyMatch(predicate -> predicate.test(poiType));
    private final Map<Block, PoiType> BLOCK_POI_MAPPING = new HashMap<>();
    private final MinecraftServer server;

    private final MapSaveData mapSaveData = new MapSaveData();

    private MarkedBlockMap(MinecraftServer server) {
        this.server = server;
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

    public static boolean put(GlobalPos pos, UUID uuid) {
        BlockState blockState = INSTANCE.server.getLevel(pos.dimension()).getBlockState(pos.pos());
        Block block = blockState.getBlock();

        if (INSTANCE.POI_BLOCKS.stream().anyMatch(block::equals)) {
            INSTANCE.markedMap.put(pos, Pair.of(uuid, blockState));
            INSTANCE.uuidMarkedMap.put(uuid, pos);
            setDirty(pos);

            return true;
        }

        return false;
    }

    public static boolean remove(GlobalPos pos) {
        UUID uuid = INSTANCE.markedMap.remove(pos).getFirst();
        INSTANCE.uuidMarkedMap.remove(uuid, pos);
        setDirty(pos);

        return uuid != null;
    }

    public static boolean contains(GlobalPos pos) {
        return INSTANCE.markedMap.containsKey(pos);
    }

    public static UUID get(GlobalPos pos) {
        return INSTANCE.markedMap.get(pos).getFirst();
    }

    public static Set<BlockPos> getPosMarkedByUUID(UUID uuid) {
        return INSTANCE.uuidMarkedMap.get(uuid).stream().map(GlobalPos::pos).collect(Collectors.toSet());
    }

    public static Set<BlockPos> getPosMarkedByOtherUUID(UUID uuid) {
        Multimap<UUID, GlobalPos> removedUUID = HashMultimap.create(INSTANCE.uuidMarkedMap);
        removedUUID.removeAll(uuid);
        return removedUUID.values().stream().map(GlobalPos::pos).collect(Collectors.toSet());
    }

    private static void setDirty(GlobalPos pos) {
        INSTANCE.mapSaveData.setDirty();
        int range = Helper.getMaxBlockRenderRange(INSTANCE.server.getPlayerList().getViewDistance());
        Set<ServerPlayer> players = ServerHelper.getPlayersInRange(INSTANCE.server, pos, range);
        for(ServerPlayer player : players) {
            ServerHelper.Network.sendMarkedBlocksUpdate(player, range);
        }
    }

    private static void clearBadEntries() {
        if (INSTANCE == null) {
            return;
        }
        Set<GlobalPos> markedForRemoval = new HashSet<>();
        for(Map.Entry<GlobalPos, Pair<UUID, BlockState>> entry : INSTANCE.markedMap.entrySet()) {
            BlockPos blockPos = entry.getKey().pos();
            BlockState blockState = entry.getValue().getSecond();
            Block block = blockState.getBlock();
            ServerLevel level = INSTANCE.server.getLevel(entry.getKey().dimension());

            if (!level.getPoiManager().exists(blockPos, INSTANCE.BLOCK_POI_MAPPING.get(block).getPredicate())) {
                markedForRemoval.add(entry.getKey());
            }
        }

        for(GlobalPos pos : markedForRemoval) {
            INSTANCE.markedMap.remove(pos);
        }
        INSTANCE.mapSaveData.setDirty();
    }

    public static void init(MinecraftServer server) {
        INSTANCE = new MarkedBlockMap(server);
        DimensionDataStorage dimensionDataStorage = server.overworld().getDataStorage();
        dimensionDataStorage.computeIfAbsent(INSTANCE.mapSaveData::load, () -> INSTANCE.mapSaveData, Constants.MODID);
    }

    public static boolean testPoi(PoiType toTest) {
        return INSTANCE.POI_TEST.test(toTest);
    }

    private static class MapSaveData extends SavedData {
        @Override
        public CompoundTag save(CompoundTag compoundTag) {
            clearBadEntries();
            ListTag listTag = new ListTag();
            for(Map.Entry<GlobalPos, Pair<UUID, BlockState>> entry : INSTANCE.markedMap.entrySet()) {
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

        public MapSaveData load(CompoundTag compoundTag) {
            List<Pair<GlobalPos, Pair<UUID, BlockState>>> list =  compoundTag.getList("MarkedBlockData", Tag.TAG_COMPOUND).stream()
                    .map(tag -> Pair.of(GlobalPos.CODEC.parse(NbtOps.INSTANCE, ((CompoundTag)tag).get("globalPos")).resultOrPartial(error -> HandsOff.logger.error(error)).get(), Pair.of(((CompoundTag)tag).getUUID("uuid"), NbtUtils.readBlockState(((CompoundTag)tag).getCompound("blockState")))))
                    .toList();

            INSTANCE.markedMap.putAll(list.stream().collect(Pair.toMap()));
                list.forEach(entry -> INSTANCE.uuidMarkedMap.put(entry.getSecond().getFirst(), entry.getFirst()));

            return INSTANCE.mapSaveData;
        }
    }
}
