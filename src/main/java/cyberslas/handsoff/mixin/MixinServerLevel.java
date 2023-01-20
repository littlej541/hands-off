package cyberslas.handsoff.mixin;

import cyberslas.handsoff.server.MarkedBlockManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel {
    @Shadow
    public abstract ServerLevel getLevel();

    @Inject(at = @At(value = "HEAD"), method = "*(Lnet/minecraft/core/BlockPos;)V")
    private void checkIfPoiRemoved(BlockPos blockPos, CallbackInfo ci) {
        MarkedBlockManager.remove(GlobalPos.of(this.getLevel().dimension(), blockPos));
    }
}
