package cyberslas.handsoff.mixin;

import com.mojang.datafixers.util.Pair;
import cyberslas.handsoff.server.MarkedBlockManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.AcquirePoi;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(AcquirePoi.class)
public abstract class MixinAcquirePoi {
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/village/poi/PoiManager;findAllClosestFirstWithType(Ljava/util/function/Predicate;Ljava/util/function/Predicate;Lnet/minecraft/core/BlockPos;ILnet/minecraft/world/entity/ai/village/poi/PoiManager$Occupancy;)Ljava/util/stream/Stream;"), method = "start(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/PathfinderMob;J)V")
    private Stream<Pair<Holder<PoiType>, BlockPos>> filterMarked(PoiManager instance, Predicate<Holder<PoiType>> poiTypePredicate, Predicate<BlockPos> blockPosPredicate, BlockPos position, int distance, PoiManager.Occupancy occupancyAllowed, ServerLevel level) {
        return instance.findAllClosestFirstWithType(poiTypePredicate, blockPosPredicate, position, distance, occupancyAllowed).filter((pos) -> !MarkedBlockManager.contains(GlobalPos.of(level.dimension(), pos.getSecond())));
    }
}
