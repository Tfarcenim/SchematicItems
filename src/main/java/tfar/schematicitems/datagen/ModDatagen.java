package tfar.schematicitems.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;

public class ModDatagen {

    public static void gather(GatherDataEvent e) {
        DataGenerator dataGenerator = e.getGenerator();
        //PackOutput packOutput = dataGenerator.getPackOutput();
        ExistingFileHelper existingFileHelper = e.getExistingFileHelper();
        boolean client = e.includeClient();
        dataGenerator.addProvider(client,new ModModelProvider(dataGenerator,existingFileHelper));
        dataGenerator.addProvider(client,new ModLangProvider(dataGenerator));
    }
}
