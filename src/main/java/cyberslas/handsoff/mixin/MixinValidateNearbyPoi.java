package cyberslas.handsoff.mixin;

import cyberslas.handsoff.server.MarkedBlockManager;
import cyberslas.handsoff.server.util.ServerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.ValidateNearbyPoi;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

@Mixin(ValidateNearbyPoi.class)
public abstract class MixinValidateNearbyPoi {
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/behavior/ValidateNearbyPoi;poiDoesntExist(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)Z"), method = "start")
    private boolean checkIfMarked(ValidateNearbyPoi instance, ServerLevel level, BlockPos pos) {
        return this.poiDoesntExist(level, pos) || MarkedBlockManager.contains(GlobalPos.of(level.dimension(), pos));
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/village/poi/PoiManager;exists(Lnet/minecraft/core/BlockPos;Ljava/util/function/Predicate;)Z"), method = "poiDoesntExist")
    private boolean removeIfExists(PoiManager instance, BlockPos pos, Predicate<Holder<PoiType>> poiTypePredicate, ServerLevel level) {
        return ServerHelper.removeFromBlockOwnershipMapIfExists(instance, GlobalPos.of(level.dimension(), pos), poiTypePredicate);
    }

    @Shadow
    protected abstract boolean poiDoesntExist(ServerLevel p_24528_, BlockPos p_24529_);
}
