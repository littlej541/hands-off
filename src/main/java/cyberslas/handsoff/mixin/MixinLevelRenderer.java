package cyberslas.handsoff.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LevelRenderer.class)
public interface MixinLevelRenderer {
    @Invoker
    void invokeRenderShape(PoseStack poseStack, VertexConsumer vertexConsumer, VoxelShape voxelShape, double xInCameraCoordinates, double yInCameraCoordinates, double zInCameraCoordinates, float red, float green, float blue, float alpha);
}
