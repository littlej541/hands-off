package cyberslas.handsoff.mixin.compat.rubidium;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import cyberslas.handsoff.client.event.ClientEventHandler;
import cyberslas.handsoff.compat.rubidium.RubidiumLastPoseStackGetter;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSectionManager.class)
public abstract class MixinRenderSectionManager {
    @Inject(method = "renderLayer", at = @At(value = "TAIL"), remap = false)
    void fireForgeEvent(ChunkRenderMatrices matrices, BlockRenderPass pass, double x, double y, double z, CallbackInfo ci) {
        if (pass.getLayer().equals(RenderType.tripwire())) {
            LevelRenderer levelRenderer = Minecraft.getInstance().levelRenderer;
            PoseStack lastPoseStack = ((RubidiumLastPoseStackGetter) SodiumWorldRenderer.instance()).getLastPoseStack();
            MixinLevelRendererInterface levelRendererInterface = (MixinLevelRendererInterface) levelRenderer;
            Frustum frustum = levelRendererInterface.getCapturedFrustum() != null ? levelRendererInterface.getCapturedFrustum() : levelRendererInterface.getCullingFrustum();
            RenderLevelStageEvent.Stage stage = RenderLevelStageEvent.Stage.fromRenderType(pass.getLayer());

            var profiler = Minecraft.getInstance().getProfiler();
            profiler.push(stage.toString());
            ClientEventHandler.renderOutline(new RenderLevelStageEvent(stage, levelRenderer, lastPoseStack, RenderSystem.getProjectionMatrix(), levelRendererInterface.getTicks(), MinecraftForgeClient.getPartialTick(), Minecraft.getInstance().gameRenderer.getMainCamera(), frustum));
            profiler.pop();
        }
    }
}
