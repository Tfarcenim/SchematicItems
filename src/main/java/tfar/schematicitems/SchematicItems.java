package tfar.schematicitems;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
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
import org.stringtemplate.v4.ST;
import tfar.schematicitems.datagen.ModDatagen;
import tfar.schematicitems.world.SlowPasteSavedData;

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
        bus.addListener(ModDatagen::gather);
        //bus.addListener(this::register);
        MinecraftForge.EVENT_BUS.addListener(this::worldTick);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommand);
    }

    private void register(RegisterEvent e) {
        e.register(Registry.ITEM_REGISTRY,new ResourceLocation(MODID,"schematic_item"),() -> Items.SCHEMATIC_ITEM);
    }

    private void worldTick(TickEvent.LevelTickEvent e) {
        if (e.phase == TickEvent.Phase.START && e.level instanceof ServerLevel serverLevel) {
            SlowPasteSavedData slowPasteSavedData = SlowPasteSavedData.loadFromLevel(serverLevel);
            slowPasteSavedData.tick(serverLevel);
        }
    }

    private void setup(final FMLCommonSetupEvent event) {

    }

    private void registerCommand(RegisterCommandsEvent e) {
        SlowLoadSchematicCommand.register(e.getDispatcher());
    }

    public static class Items {
        public static final Item SCHEMATIC_ITEM = new SchematicItem(new Item.Properties());
    }

}
