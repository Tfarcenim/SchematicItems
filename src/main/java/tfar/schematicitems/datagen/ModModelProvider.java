package tfar.schematicitems.datagen;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import tfar.schematicitems.SchematicItems;

public class ModModelProvider extends ItemModelProvider {
    public ModModelProvider(DataGenerator output, ExistingFileHelper existingFileHelper) {
        super(output, SchematicItems.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        makeOneLayerItem(SchematicItems.Items.SCHEMATIC_ITEM);
    }

    protected void makeSimpleItem(Item item, ResourceLocation loc) {
        String s = Registry.ITEM.getKey(item).toString();

        getBuilder(s).parent(getExistingFile(loc));
    }

    protected void makeSimpleItem(Item item) {
        makeSimpleItem(item, new ResourceLocation(SchematicItems.MODID, "item/" + Registry.ITEM.getKey(item).getPath()));
    }

    protected void makeOneLayerItem(Item item, ResourceLocation texture) {
        String path = Registry.ITEM.getKey(item).getPath();
        if (existingFileHelper.exists(new ResourceLocation(texture.getNamespace(), "item/" + texture.getPath())
                , PackType.CLIENT_RESOURCES, ".png", "textures")) {
            getBuilder(path).parent(getExistingFile(mcLoc("item/generated")))
                    .texture("layer0", new ResourceLocation(texture.getNamespace(), "item/" + texture.getPath()));
        } else {
            System.out.println("no texture for " + item + " found, skipping");
        }
    }

    protected void makeOneLayerItem(Item item) {
        ResourceLocation texture = Registry.ITEM.getKey(item);
        makeOneLayerItem(item, texture);
    }

}
