package cyberslas.handsoff.mixin.compat.rubidium;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public interface MixinLevelRendererInterface {
    @Accessor
    Frustum getCullingFrustum();

    @Accessor
    Frustum getCapturedFrustum();

    @Accessor
    int getTicks();
}
