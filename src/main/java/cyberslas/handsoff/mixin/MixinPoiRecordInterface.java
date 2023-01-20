package cyberslas.handsoff.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PoiRecord.class)
public interface MixinPoiRecordInterface {
    @Accessor
    Runnable getSetDirty();

    @Accessor
    void setFreeTickets(int count);

    @Accessor
    Holder<PoiType> getPoiType();
}
