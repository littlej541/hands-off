package cyberslas.handsoff.registry;

import cyberslas.handsoff.item.BlockMarkerItem;
import cyberslas.handsoff.util.Constants;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Items {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MODID);

    public static final RegistryObject<Item> BLOCK_MARKER = ITEMS.register("block_marker", () -> new BlockMarkerItem(new Item.Properties().durability(0).tab(CreativeModeTab.TAB_TOOLS)));
}
