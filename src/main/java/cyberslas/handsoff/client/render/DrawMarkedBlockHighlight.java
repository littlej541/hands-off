package cyberslas.handsoff.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Vector4f;
import cyberslas.handsoff.config.Config;
import cyberslas.handsoff.mixin.MixinLevelRenderer;
import cyberslas.handsoff.util.Helper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashSet;
import java.util.OptionalDouble;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class DrawMarkedBlockHighlight {
    private static DrawMarkedBlockHighlight INSTANCE;

    private final MultiBufferSource.BufferSource bufferSource;
    private final Minecraft minecraft;
    private final Set<BlockPos> unlockedPositions = new HashSet<>();
    private final Set<BlockPos> lockedPositions = new HashSet<>();

    public static void setBlockPositions(Set<BlockPos> newUnlockedPositions, Set<BlockPos> newLockedPositions) {
        INSTANCE.unlockedPositions.clear();
        INSTANCE.unlockedPositions.addAll(newUnlockedPositions);
        INSTANCE.lockedPositions.clear();
        INSTANCE.lockedPositions.addAll(newLockedPositions);
    }

    public static void init(Minecraft mc) {
        INSTANCE = new DrawMarkedBlockHighlight(mc);
        RenderOutline.create();
    }

    private DrawMarkedBlockHighlight(Minecraft mc) {
        this.bufferSource = mc.renderBuffers().bufferSource();
        this.minecraft = mc;
    }

    public static void render(LevelRenderer levelRenderer, Camera camera, PoseStack poseStack) {
        LocalPlayer player = (LocalPlayer)camera.getEntity();
        Level level = player.getLevel();
        Vec3 cameraPosition = camera.getPosition();

        VertexConsumer vertexConsumer = INSTANCE.bufferSource.getBuffer(RenderOutline.renderType);
        drawOutlines(levelRenderer, poseStack, vertexConsumer, player, level, INSTANCE.unlockedPositions, cameraPosition, RenderOutline.unlockedColor);
        drawOutlines(levelRenderer, poseStack, vertexConsumer, player, level, INSTANCE.lockedPositions, cameraPosition, RenderOutline.lockedColor);
        INSTANCE.bufferSource.endBatch(RenderOutline.renderType);
    }

    private static void drawOutlines(LevelRenderer levelRenderer, PoseStack poseStack, VertexConsumer vertexConsumer, LocalPlayer player, Level level, Set<BlockPos> outlines, Vec3 cameraPosition, Vector4f color) {
        for(BlockPos pos : outlines) {
            if (Config.CLIENT.showOutlines.get() && Math.sqrt(pos.distToCenterSqr(player.position())) <= RenderOutline.renderRange && Helper.playerHoldingBlockMarker(player)) {
                VoxelShape voxelShape = level.getBlockState(pos).getShape(level, pos, CollisionContext.of(player));
                ((MixinLevelRenderer) levelRenderer).invokeRenderShape(poseStack, vertexConsumer, voxelShape, pos.getX() - cameraPosition.x(), pos.getY() - cameraPosition.y(), pos.getZ() - cameraPosition.z(), color.x(), color.y(), color.z(), color.w());
            }
        }
    }

    private static class RenderOutline extends RenderType {
        private static RenderType renderType;
        private static Vector4f unlockedColor;
        private static Vector4f lockedColor;
        private static int renderRange;

        private RenderOutline(String p_173178_, VertexFormat p_173179_, VertexFormat.Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_) {
            super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
        }

        private static void create() {
            unlockedColor = new Vector4f(Config.CLIENT.unlockedOutlinesRed.get(),  Config.CLIENT.unlockedOutlinesGreen.get(), Config.CLIENT.unlockedOutlinesBlue.get(), Config.CLIENT.unlockedOutlinesAlpha.get());
            unlockedColor.mul(1.0f / 255.0f);
            lockedColor = new Vector4f(Config.CLIENT.lockedOutlinesRed.get(),  Config.CLIENT.lockedOutlinesGreen.get(), Config.CLIENT.lockedOutlinesBlue.get(), Config.CLIENT.lockedOutlinesAlpha.get());
            lockedColor.mul(1.0f / 255.0f);
            renderRange = Math.min(Helper.getMaxBlockRenderRange(INSTANCE.minecraft.options.getEffectiveRenderDistance()), Config.CLIENT.drawOutlinesRange.get());

            renderType = RenderType.create("block_outline",
                    DefaultVertexFormat.POSITION_COLOR_NORMAL,
                    VertexFormat.Mode.LINES,
                    256,
                    false,
                    false,
                    RenderType.CompositeState.builder()
                            .setShaderState(RENDERTYPE_LINES_SHADER)
                            .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(Config.CLIENT.outlinesThickness.get())))
                            .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setOutputState(ITEM_ENTITY_TARGET)
                            .setWriteMaskState(COLOR_DEPTH_WRITE)
                            .setCullState(NO_CULL)
                            .createCompositeState(false));
        }
    }
}
