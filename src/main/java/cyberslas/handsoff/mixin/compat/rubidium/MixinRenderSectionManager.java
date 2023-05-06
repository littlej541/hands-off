package cyberslas.handsoff.mixin.compat.rubidium;

import com.mojang.blaze3d.systems.RenderSystem;
import cyberslas.handsoff.client.event.ClientEventHandler;
import cyberslas.handsoff.compat.rubidium.RubidiumPoseStackCapturer;
import cyberslas.handsoff.mixin.MixinLevelRendererAccessor;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSectionManager.class)
public abstract class MixinRenderSectionManager {
    @Inject(method = "renderLayer", at = @At(value = "TAIL"), remap = false)
    void pseudoFireForgeRenderLevelStageEvent(ChunkRenderMatrices matrices, BlockRenderPass pass, double x, double y, double z, CallbackInfo ci) {
        if (pass == BlockRenderPass.TRIPWIRE) {
            Minecraft minecraft = Minecraft.getInstance();
            MixinLevelRendererAccessor levelRendererInterface = (MixinLevelRendererAccessor) minecraft.levelRenderer;
            Frustum frustum = levelRendererInterface.getCapturedFrustum() != null ? levelRendererInterface.getCapturedFrustum() : levelRendererInterface.getCullingFrustum();
            RenderLevelStageEvent.Stage stage = RenderLevelStageEvent.Stage.fromRenderType(pass.getLayer());

            var profiler = minecraft.getProfiler();
            profiler.push(stage.toString());
            ClientEventHandler.renderOutline(new RenderLevelStageEvent(stage, minecraft.levelRenderer, ((RubidiumPoseStackCapturer) SodiumWorldRenderer.instance()).getCapturedPoseStack(), RenderSystem.getProjectionMatrix(), levelRendererInterface.getTicks(), minecraft.getPartialTick(), minecraft.gameRenderer.getMainCamera(), frustum));
            profiler.pop();
        }
    }
}
