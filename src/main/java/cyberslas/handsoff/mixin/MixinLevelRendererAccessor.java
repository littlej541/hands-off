package cyberslas.handsoff.mixin;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelRenderer.class)
public interface MixinLevelRendererAccessor {
    @Accessor
    Frustum getCullingFrustum();

    @Accessor
    Frustum getCapturedFrustum();

    @Accessor
    int getTicks();
}
