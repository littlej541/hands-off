package cyberslas.handsoff.mixin;

import cyberslas.handsoff.server.MarkedBlockManager;
import cyberslas.handsoff.server.util.ServerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.GoToPotentialJobSite;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.Predicate;

@Mixin(GoToPotentialJobSite.class)
public abstract class MixinGoToPotentialJobSite {
    private static ServerLevel serverLevel;

    @Inject(at = @At(value = "TAIL"), method = "canStillUse(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;J)Z", cancellable = true)
    private void checkIfMarked(ServerLevel level, Villager villager, long startTimeMaybe, CallbackInfoReturnable<Boolean> cir) {
        Optional<GlobalPos> optionalGlobalPos = villager.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE);

        if (optionalGlobalPos.isPresent()) {
            GlobalPos pos = optionalGlobalPos.get();

            if (MarkedBlockManager.contains(pos)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/village/poi/PoiManager;exists(Lnet/minecraft/core/BlockPos;Ljava/util/function/Predicate;)Z"), method = "*(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/GlobalPos;)V")
    private static boolean removeIfExists(PoiManager instance, BlockPos pos, Predicate<PoiType> poiTypePredicate) {
        return ServerHelper.removeFromBlockOwnershipMapIfExists(instance, GlobalPos.of(serverLevel.dimension(), pos), poiTypePredicate);
    }

    @Inject(at = @At(value = "HEAD"), method = "stop(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;J)V")
    private void forgetWalkToMemoryAndStoreLevel(ServerLevel level, Villager villager, long startTimeMaybe, CallbackInfo ci) {
        serverLevel = level;
        villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }
}
