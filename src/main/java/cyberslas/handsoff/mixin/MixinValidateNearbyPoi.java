package cyberslas.handsoff.mixin;

import com.mojang.datafixers.kinds.IdF;
import cyberslas.handsoff.server.MarkedBlockManager;
import cyberslas.handsoff.server.util.ServerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.ValidateNearbyPoi;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(ValidateNearbyPoi.class)
public abstract class MixinValidateNearbyPoi {
    private static ServerLevel serverLevel;

    @Inject(at = @At(value = "HEAD", target = "Lnet/minecraft/world/entity/ai/village/poi/PoiManager;exists(Lnet/minecraft/core/BlockPos;Ljava/util/function/Predicate;)Z"), method = "*(Lnet/minecraft/world/entity/ai/behavior/declarative/BehaviorBuilder$Instance;Lnet/minecraft/world/entity/ai/behavior/declarative/MemoryAccessor;Ljava/util/function/Predicate;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;J)Z")
    private static void captureLevel(BehaviorBuilder.Instance<LivingEntity> builder, MemoryAccessor<IdF.Mu, GlobalPos> globalPosMemoryAccessor, Predicate<Holder<PoiType>> poiTypePredicate, ServerLevel level, LivingEntity entity, long startTimeMaybe, CallbackInfoReturnable<Boolean> cir) {
        serverLevel = level;
    }
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/village/poi/PoiManager;exists(Lnet/minecraft/core/BlockPos;Ljava/util/function/Predicate;)Z"), method = "*(Lnet/minecraft/world/entity/ai/behavior/declarative/BehaviorBuilder$Instance;Lnet/minecraft/world/entity/ai/behavior/declarative/MemoryAccessor;Ljava/util/function/Predicate;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;J)Z")
    private static boolean checkIfMarked(PoiManager instance, BlockPos pos, Predicate<Holder<PoiType>> poiTypePredicate) {
        return ServerHelper.removeFromBlockOwnershipMapIfExists(instance, GlobalPos.of(serverLevel.dimension(), pos), poiTypePredicate) || MarkedBlockManager.contains(GlobalPos.of(serverLevel.dimension(), pos));
    }
}
