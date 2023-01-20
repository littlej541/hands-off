package cyberslas.handsoff.mixin;

import cyberslas.handsoff.server.MarkedBlockManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.SetClosestHomeAsWalkTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.stream.Stream;

@Mixin(SetClosestHomeAsWalkTarget.class)
public abstract class MixinSetClosestHomeAsWalkTarget {
    private static ServerLevel serverLevel;

    @Inject(at = @At(value = "HEAD"), method = "start")
    private void storeLevel(ServerLevel level, LivingEntity livingEntity, long startTimeMaybe, CallbackInfo ci) {
        serverLevel = level;
    }

    @ModifyVariable(at = @At(value = "STORE"), ordinal = 0, method = "start")
    private Stream<BlockPos> filterMarked(Stream<BlockPos> value) {
        return value.filter((pos) -> !MarkedBlockManager.contains(GlobalPos.of(serverLevel.dimension(), pos)));
    }
}
