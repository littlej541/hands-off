package cyberslas.handsoff.mixin;

import cyberslas.handsoff.server.MarkedBlockMap;
import cyberslas.handsoff.server.util.ServerHelper;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.AssignProfessionFromJobSite;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(AssignProfessionFromJobSite.class)
public abstract class MixinAssignProfessionFromJobSite {
    @Inject(at = @At(value = "HEAD"), method = "start(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;J)V", cancellable = true)
    private void checkIfMarked(ServerLevel level, Villager villager, long startTimeMaybe, CallbackInfo ci) {
        Optional<GlobalPos> globalPosOptional = villager.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE);

        if (globalPosOptional.isPresent()) {
            GlobalPos pos = globalPosOptional.get();

            if (MarkedBlockMap.contains(pos)) {
                ServerHelper.clearPoiAndMemory(villager, MemoryModuleType.POTENTIAL_JOB_SITE);

                ci.cancel();
            }
        }
    }
}
