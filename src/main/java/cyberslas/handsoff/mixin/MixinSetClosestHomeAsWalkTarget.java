package cyberslas.handsoff.mixin;

import com.mojang.datafixers.util.Pair;
import cyberslas.handsoff.server.MarkedBlockManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.SetClosestHomeAsWalkTarget;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(SetClosestHomeAsWalkTarget.class)
public abstract class MixinSetClosestHomeAsWalkTarget {
    private static ServerLevel serverLevel;

    @Inject(at = @At(value = "HEAD"), method = "start")
    private void storeLevel(ServerLevel level, LivingEntity livingEntity, long startTimeMaybe, CallbackInfo ci) {
        serverLevel = level;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/village/poi/PoiManager;findAllWithType(Ljava/util/function/Predicate;Ljava/util/function/Predicate;Lnet/minecraft/core/BlockPos;ILnet/minecraft/world/entity/ai/village/poi/PoiManager$Occupancy;)Ljava/util/stream/Stream;"), method = "start")
    private Stream<Pair<Holder<PoiType>, BlockPos>> filterMarked(PoiManager instance, Predicate<Holder<PoiType>> poiTypePredicate, Predicate<BlockPos> blockPosPredicate, BlockPos position, int distance, PoiManager.Occupancy occupancyAllowed, ServerLevel level) {
        return instance.findAllClosestFirstWithType(poiTypePredicate, blockPosPredicate, position, distance, occupancyAllowed).filter((pos) -> !MarkedBlockManager.contains(GlobalPos.of(level.dimension(), pos.getSecond())));
    }
}
