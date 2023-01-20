package cyberslas.handsoff.mixin;

import com.mojang.datafixers.kinds.Const;
import com.mojang.datafixers.kinds.IdF;
import com.mojang.datafixers.kinds.OptionalBox;
import com.mojang.datafixers.util.Unit;
import cyberslas.handsoff.server.MarkedBlockManager;
import cyberslas.handsoff.server.util.ServerHelper;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromBlockMemory;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SetWalkTargetFromBlockMemory.class)
public abstract class MixinSetWalkTargetFromBlockMemory {
    @Inject(at = @At(value = "HEAD"), method = "*(Lnet/minecraft/world/entity/ai/behavior/declarative/BehaviorBuilder$Instance;Lnet/minecraft/world/entity/ai/behavior/declarative/MemoryAccessor;Lnet/minecraft/world/entity/ai/behavior/declarative/MemoryAccessor;IILnet/minecraft/world/entity/ai/memory/MemoryModuleType;Lnet/minecraft/world/entity/ai/behavior/declarative/MemoryAccessor;FILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;J)Z", cancellable = true)
    private static void checkIfMarked(BehaviorBuilder.Instance<Villager> builder, MemoryAccessor<IdF.Mu, GlobalPos> globalPosMemoryAccessor, MemoryAccessor<OptionalBox.Mu, Long> cantReachWalkTargetSinceMemoryAccessor, int tooLongUnreachableDuration, int tooFarDistance, MemoryModuleType<GlobalPos> globalPosMemoryModuleType, MemoryAccessor<Const.Mu<Unit>, WalkTarget> walkTargetMemoryAccessor, float speedModifier, int closeEnoughDist, ServerLevel level, Villager villager, long startTimeMaybe, CallbackInfoReturnable<Boolean> cir) {
        GlobalPos globalPos = builder.get(globalPosMemoryAccessor);
        if (MarkedBlockManager.contains(globalPos)) {
            ServerHelper.clearPoiAndMemory(villager, globalPosMemoryModuleType, globalPosMemoryAccessor);
            cir.setReturnValue(true);
        }
    }
}
