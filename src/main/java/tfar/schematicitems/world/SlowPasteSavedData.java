package tfar.schematicitems.world;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import tfar.schematicitems.SchematicItems;

import java.util.ArrayList;
import java.util.List;

public class SlowPasteSavedData extends SavedData {

    private final List<SlowPaste> pastes = new ArrayList<>();

    @Override
    public CompoundTag save(CompoundTag pCompoundTag) {
        ListTag listTag = new ListTag();
        for (SlowPaste slowPaste : pastes) {
            listTag.add(slowPaste.save());
        }
        pCompoundTag.put("pastes",listTag);
        return pCompoundTag;
    }

    public static SlowPasteSavedData loadStatic(CompoundTag compoundTag) {
        SlowPasteSavedData SlowPasteSavedData = new SlowPasteSavedData();
        SlowPasteSavedData.load(compoundTag);
        return SlowPasteSavedData;
    }

    public static SlowPasteSavedData loadFromLevel(ServerLevel level) {
        return level.getDataStorage()
                .computeIfAbsent(SlowPasteSavedData::loadStatic, SlowPasteSavedData::new, SchematicItems.MODID+"_"+level.dimension().location());
    }

    public void addSlowPasteData(BlockPos start, Clipboard clipboard, int speed) {
        SlowPaste slowPaste = SlowPaste.create(start,clipboard,speed);
        pastes.add(slowPaste);
        setDirty();
    }

    public void tick(ServerLevel level) {
        for (SlowPaste slowPaste : pastes) {
            slowPaste.tick(level);
        }
        pastes.removeIf(SlowPaste::finished);
    }

    protected void load(CompoundTag compoundTag) {
        ListTag listTag = compoundTag.getList("pastes", Tag.TAG_COMPOUND);
        for (Tag tag : listTag) {
            CompoundTag tag1 = (CompoundTag)tag;
            SlowPaste slowPaste = SlowPaste.loadFromTag(tag1);
            pastes.add(slowPaste);
        }
    }
}
