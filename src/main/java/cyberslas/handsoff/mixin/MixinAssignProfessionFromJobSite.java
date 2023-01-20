package cyberslas.handsoff.mixin;

import com.mojang.datafixers.kinds.IdF;
import com.mojang.datafixers.kinds.OptionalBox;
import cyberslas.handsoff.server.MarkedBlockManager;
import cyberslas.handsoff.server.util.ServerHelper;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.AssignProfessionFromJobSite;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AssignProfessionFromJobSite.class)
public abstract class MixinAssignProfessionFromJobSite {
    @Inject(at = @At(value = "HEAD"), method = "*(Lnet/minecraft/world/entity/ai/behavior/declarative/BehaviorBuilder$Instance;Lnet/minecraft/world/entity/ai/behavior/declarative/MemoryAccessor;Lnet/minecraft/world/entity/ai/behavior/declarative/MemoryAccessor;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;J)Z", cancellable = true)
    private static void checkIfMarked(BehaviorBuilder.Instance<Villager> builder, MemoryAccessor<IdF.Mu, GlobalPos> potentialJobSiteMemoryAccessor, MemoryAccessor<OptionalBox.Mu, GlobalPos> jobSiteMemoryAccessor, ServerLevel level, Villager villager, long startTimeMaybe, CallbackInfoReturnable<Boolean> cir) {
        GlobalPos globalPos = builder.get(potentialJobSiteMemoryAccessor);

        if (MarkedBlockManager.contains(globalPos)) {
            ServerHelper.clearPoiAndMemory(villager, MemoryModuleType.POTENTIAL_JOB_SITE, potentialJobSiteMemoryAccessor);

            cir.setReturnValue(true);
        }
    }
}
