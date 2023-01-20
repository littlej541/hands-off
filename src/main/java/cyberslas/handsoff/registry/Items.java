package cyberslas.handsoff.registry;

import cyberslas.handsoff.item.BlockMarkerItem;
import cyberslas.handsoff.util.Constants;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Items {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MODID);

    public static final RegistryObject<Item> BLOCK_MARKER = ITEMS.register("block_marker", () -> new BlockMarkerItem(new Item.Properties().durability(0)));

    @SubscribeEvent
    public static void buildCreativeTabContents(CreativeModeTabEvent.BuildContents event) {
        // Add to ingredients tab
        if (event.getTab() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(BLOCK_MARKER);
        }
    }
}
