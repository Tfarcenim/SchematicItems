package tfar.schematicitems.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;
import tfar.schematicitems.SchematicItems;

public class ModLangProvider extends LanguageProvider {
    public ModLangProvider(DataGenerator output) {
        super(output, SchematicItems.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add(SchematicItems.Items.SCHEMATIC_ITEM,"Placeable Schematic");
    }
}
