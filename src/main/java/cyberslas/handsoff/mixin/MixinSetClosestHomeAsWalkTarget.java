package cyberslas.handsoff.mixin;

import com.mojang.datafixers.kinds.Const;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import cyberslas.handsoff.server.MarkedBlockManager;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.SetClosestHomeAsWalkTarget;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import org.apache.commons.lang3.mutable.MutableLong;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(SetClosestHomeAsWalkTarget.class)
public abstract class MixinSetClosestHomeAsWalkTarget {
    private static ServerLevel serverLevel;

    @Inject(at = @At(value = "HEAD"), method = "*(Lorg/apache/commons/lang3/mutable/MutableLong;Lit/unimi/dsi/fastutil/longs/Long2LongMap;Lnet/minecraft/world/entity/ai/behavior/declarative/MemoryAccessor;FLnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/PathfinderMob;J)Z")
    private static void captureLevel(MutableLong mutablelong, Long2LongMap long2longmap, MemoryAccessor<Const.Mu<Unit>, GlobalPos> walkTargetMemoryAccessor, float speedModifier, ServerLevel level, PathfinderMob pathfinderMob, long startTimeMaybe, CallbackInfoReturnable<Boolean> cir) {
        serverLevel = level;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/village/poi/PoiManager;findAllWithType(Ljava/util/function/Predicate;Ljava/util/function/Predicate;Lnet/minecraft/core/BlockPos;ILnet/minecraft/world/entity/ai/village/poi/PoiManager$Occupancy;)Ljava/util/stream/Stream;"), method = "*(Lorg/apache/commons/lang3/mutable/MutableLong;Lit/unimi/dsi/fastutil/longs/Long2LongMap;Lnet/minecraft/world/entity/ai/behavior/declarative/MemoryAccessor;FLnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/PathfinderMob;J)Z")
    private static Stream<Pair<Holder<PoiType>, BlockPos>> filterMarked(PoiManager instance, Predicate<Holder<PoiType>> poiTypePredicate, Predicate<BlockPos> blockPosPredicate, BlockPos position, int distance, PoiManager.Occupancy occupancyAllowed) {
        return instance.findAllClosestFirstWithType(poiTypePredicate, blockPosPredicate, position, distance, occupancyAllowed).filter((pos) -> !MarkedBlockManager.contains(GlobalPos.of(serverLevel.dimension(), pos.getSecond())));
    }
}
