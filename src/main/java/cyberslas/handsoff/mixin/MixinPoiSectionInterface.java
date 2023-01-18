package cyberslas.handsoff.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(PoiSection.class)
public interface MixinPoiSectionInterface {
    @Accessor
    Runnable getSetDirty();

    @Invoker
    Optional<PoiRecord> invokeGetPoiRecord(BlockPos blockPos);
}
