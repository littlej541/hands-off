package cyberslas.handsoff.mixin;

import com.mojang.datafixers.kinds.Const;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import cyberslas.handsoff.server.MarkedBlockManager;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.AcquirePoi;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import org.apache.commons.lang3.mutable.MutableLong;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(AcquirePoi.class)
public abstract class MixinAcquirePoi {
    private static ServerLevel serverLevel;

    @Inject(at = @At(value = "HEAD"), method = "*(ZLorg/apache/commons/lang3/mutable/MutableLong;Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;Ljava/util/function/Predicate;Lnet/minecraft/world/entity/ai/behavior/declarative/MemoryAccessor;Ljava/util/Optional;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/PathfinderMob;J)Z")
    private static void captureLevel(boolean p_259108_, MutableLong mutablelong, Long2ObjectMap<?> long2objectmap, Predicate<Holder<PoiType>> poiType, MemoryAccessor<Const.Mu<Unit>, GlobalPos> memoryToAcquireMemoryAccessor, Optional<BlockPos> blockPos, ServerLevel level, PathfinderMob pathfinderMob, long startTimeMaybe, CallbackInfoReturnable<Boolean> cir) {
        serverLevel = level;
    }
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/village/poi/PoiManager;findAllClosestFirstWithType(Ljava/util/function/Predicate;Ljava/util/function/Predicate;Lnet/minecraft/core/BlockPos;ILnet/minecraft/world/entity/ai/village/poi/PoiManager$Occupancy;)Ljava/util/stream/Stream;"), method = "*(ZLorg/apache/commons/lang3/mutable/MutableLong;Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;Ljava/util/function/Predicate;Lnet/minecraft/world/entity/ai/behavior/declarative/MemoryAccessor;Ljava/util/Optional;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/PathfinderMob;J)Z")
    private static Stream<Pair<Holder<PoiType>, BlockPos>> filterMarked(PoiManager instance, Predicate<Holder<PoiType>> poiTypePredicate, Predicate<BlockPos> blockPosPredicate, BlockPos position, int distance, PoiManager.Occupancy occupancyAllowed) {
        return instance.findAllClosestFirstWithType(poiTypePredicate, blockPosPredicate, position, distance, occupancyAllowed).filter((pos) -> !MarkedBlockManager.contains(GlobalPos.of(serverLevel.dimension(), pos.getSecond())));
    }
}
