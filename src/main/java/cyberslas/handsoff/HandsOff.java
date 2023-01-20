package cyberslas.handsoff;

import cyberslas.handsoff.config.Config;
import cyberslas.handsoff.network.NetworkHandler;
import cyberslas.handsoff.registry.Items;
import cyberslas.handsoff.util.Constants;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Constants.MODID)
public class HandsOff {
    public static Logger logger = LogManager.getLogger();

    public HandsOff() {
        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        IEventBus fmlEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modLoadingContext.registerConfig(ModConfig.Type.CLIENT, Config.CONFIG_SPEC_CLIENT);
        modLoadingContext.registerConfig(ModConfig.Type.COMMON, Config.CONFIG_SPEC_COMMON);

        fmlEventBus.addListener(this::commonSetup);
        fmlEventBus.addListener(Items::buildCreativeTabContents);

        Items.ITEMS.register(fmlEventBus);
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        NetworkHandler.init();
    }
}