package cyberslas.handsoff.mixin;

import cyberslas.handsoff.server.util.ServerHelper;
import cyberslas.handsoff.server.MarkedBlockMap;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.SetWalkTargetFromBlockMemory;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SetWalkTargetFromBlockMemory.class)
public abstract class MixinSetWalkTargetFromBlockMemory {
    @Shadow
    @Final
    private MemoryModuleType<GlobalPos> memoryType;

    @Inject(at = @At(value = "HEAD"), method = "start(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/npc/Villager;J)V", cancellable = true)
    private void checkIfMarked(ServerLevel level, Villager villager, long startTimeMaybe, CallbackInfo ci) {
        villager.getBrain().getMemory(this.memoryType).ifPresent(globalPos -> {
            if (MarkedBlockMap.contains(globalPos)) {
                ServerHelper.clearPoiAndMemory(villager, memoryType);
                ci.cancel();
            }
        });

    }
}
