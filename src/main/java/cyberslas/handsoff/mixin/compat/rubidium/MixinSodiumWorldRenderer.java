package cyberslas.handsoff.mixin.compat.rubidium;

import com.mojang.blaze3d.vertex.PoseStack;
import cyberslas.handsoff.compat.rubidium.RubidiumLastPoseStackGetter;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SodiumWorldRenderer.class)
public abstract class MixinSodiumWorldRenderer implements RubidiumLastPoseStackGetter {
    private static PoseStack lastPoseStack = new PoseStack();

    public PoseStack getLastPoseStack() {
        return lastPoseStack;
    }

    @Inject(method = "drawChunkLayer", at = @At(value = "HEAD"), remap = false)
    void captureLastPoseStack(RenderType renderLayer, PoseStack matrixStack, double x, double y, double z, CallbackInfo ci) {
        lastPoseStack = matrixStack;
    }
}
