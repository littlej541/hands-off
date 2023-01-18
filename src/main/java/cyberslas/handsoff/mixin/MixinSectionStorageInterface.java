package cyberslas.handsoff.mixin;

import net.minecraft.world.level.chunk.storage.SectionStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(SectionStorage.class)
public interface MixinSectionStorageInterface {
    @Invoker
    Optional<?> invokeGetOrLoad(long posSectionAsLong);
}
