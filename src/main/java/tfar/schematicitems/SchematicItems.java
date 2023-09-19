package tfar.schematicitems;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import org.slf4j.Logger;

import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SchematicItems.MODID)
public class SchematicItems {
    
    public static final String MODID = "schematicitems";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public SchematicItems() {
        IEventBus bus  = FMLJavaModLoadingContext.get().getModEventBus();
        // Register the setup method for modloading
        bus.addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {

    }
}
