package tfar.schematicitems;

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.PlatformManager;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.forge.ForgeAdapter;
import com.sk89q.worldedit.forge.ForgePlayer;
import com.sk89q.worldedit.forge.ForgeWorld;
import com.sk89q.worldedit.forge.ForgeWorldEdit;
import com.sk89q.worldedit.forge.internal.NBTConverter;
import com.sk89q.worldedit.internal.block.BlockStateIdAccess;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.io.Closer;
import com.sk89q.worldedit.util.io.file.FilenameException;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SchematicItem extends Item {
    public SchematicItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {

        if (pContext.getLevel().isClientSide) {
            return InteractionResult.SUCCESS;
        }


        String filename = "CAS_trampolines";

        String formatName = "sponge";

        WorldEdit worldEdit = WorldEdit.getInstance();
        LocalConfiguration config = worldEdit.getConfiguration();

        PlatformManager platformManager = worldEdit.getPlatformManager();

        ForgePlayer forgePlayer = ForgeAdapter.adaptPlayer((ServerPlayer) pContext.getPlayer());

        Actor actor = platformManager.createProxyActor(forgePlayer);


        ForgeWorldEdit forgeWorldEdit = ForgeWorldEdit.inst;

        File dir = worldEdit.getWorkingDirectoryPath(config.saveDir).toFile();
        try {
            File f = worldEdit.getSafeOpenFile(actor, dir, filename,
                    BuiltInClipboardFormat.SPONGE_SCHEMATIC.getPrimaryFileExtension(),
                    ClipboardFormats.getFileExtensionArray());

            ClipboardFormat format = ClipboardFormats.findByFile(f);
            if (format == null) {
                format = ClipboardFormats.findByAlias(formatName);
            }
            if (format == null) {
                actor.printError(TranslatableComponent.of("worldedit.schematic.unknown-format", TextComponent.of(formatName)));
                return InteractionResult.SUCCESS;
            }

            try (Closer closer = Closer.create()) {
                FileInputStream fis = closer.register(new FileInputStream(f));
                BufferedInputStream bis = closer.register(new BufferedInputStream(fis));
                ClipboardReader reader = closer.register(format.getReader(bis));

                Clipboard clipboard = reader.read();
                //LOGGER.info(actor.getName() + " loaded " + file.getCanonicalPath());
                // return new ClipboardHolder(clipboard);


                placeAll(pContext, new ClipboardHolder(clipboard));
            }


            //         SchematicCommands.SchematicLoadTask task = new SchematicCommands.SchematicLoadTask(actor, f, format);
            //       AsyncCommandBuilder.wrap(task, actor)
            //               .registerWithSupervisor(worldEdit.getSupervisor(), "Loading schematic " + filename)
            //               .setDelayMessage(TranslatableComponent.of("worldedit.schematic.load.loading"))
            //              .setWorkingMessage(TranslatableComponent.of("worldedit.schematic.load.still-loading"))
            //               .onSuccess(TextComponent.of(filename, TextColor.GOLD)
            //                               .append(TextComponent.of(" loaded. Paste it with ", TextColor.LIGHT_PURPLE))
            //                               .append(CodeFormat.wrap("//paste").clickEvent(ClickEvent.of(ClickEvent.Action.SUGGEST_COMMAND, "//paste"))),
            //                       session::setClipboard)
            //               .onFailure("Failed to load schematic", worldEdit.getPlatformManager().getPlatformCommandManager().getExceptionConverter())
            //               .buildAndExec(worldEdit.getExecutorService());

        } catch (FilenameException | IOException e) {
            e.printStackTrace();
        }
        return InteractionResult.SUCCESS;
    }

    public static void placeAll(UseOnContext context, ClipboardHolder clipboardHolder) {
        Clipboard clipboard = clipboardHolder.getClipboard();
        Level level = context.getLevel();
        BlockArrayClipboard blockArrayClipboard = (BlockArrayClipboard) clipboard;
        Region region = blockArrayClipboard.getRegion();
        BlockVector3 blockVector3min = region.getMinimumPoint();
        BlockVector3 blockVector3max = region.getMaximumPoint();

        BlockVector3 dims = blockArrayClipboard.getDimensions();

        int x1 = blockVector3min.getBlockX();
        int y1 = blockVector3min.getBlockY();
        int z1 = blockVector3min.getBlockZ();

        int x2 = blockVector3max.getBlockX();
        int y2 = blockVector3max.getBlockY();
        int z2 = blockVector3max.getBlockZ();


        blockStates = new Pair
                [dims.getX()][dims.getY()][dims.getZ()];

        for (int x = x1; x < x2+1; x++) {
            for (int y = y1; y < y2+1; y++) {
                for (int z = z1; z < z2+1; z++) {
                    BaseBlock worldEditBlockState = blockArrayClipboard.getFullBlock(BlockVector3.at(x, y, z));

                    com.sk89q.jnbt.CompoundTag compoundTag = worldEditBlockState.getNbtData();

                    int stateId = BlockStateIdAccess.getBlockStateId(worldEditBlockState.toImmutableState());
                    net.minecraft.world.level.block.state.BlockState vanillaBlockState =
                            BlockStateIdAccess.isValidInternalId(stateId) ? Block.stateById(stateId) :
                                    ForgeAdapter.adapt(worldEditBlockState.toImmutableState());

                       //vanillaBlockState = Blocks.DIAMOND_BLOCK.defaultBlockState();compoundTag = null;


                    int indexX = x-x1;
                    int indexY = y-y1;
                    int indexZ = z-z1;

                    blockStates[indexX][indexY][indexZ] = Pair.of(vanillaBlockState,compoundTag != null ? NBTConverter.toNative(compoundTag) : null);

                }
            }
        }

        progress = 0;
        SchematicItem.dimensions = new Vec3i(dims.getX(),dims.getY(),dims.getZ());
        start = context.getClickedPos().above();

        return;
    }

    static Pair<net.minecraft.world.level.block.state.BlockState, CompoundTag>[][][] blockStates;

    static Vec3i dimensions;
    static BlockPos start;
    static int progress = 0;
    public static void worldTick(TickEvent.LevelTickEvent e) {
        if (e.phase == TickEvent.Phase.START && !e.level.isClientSide && blockStates != null && dimensions != null && e.level.dimension() == Level.OVERWORLD) {
            int blockPerPass = 100;
            for (int i = 0; i < blockPerPass;i++) {
                Vec3i index = getIndex();
                if (index != null) {
                    BlockPos offset = start.offset(index);
                    Pair<net.minecraft.world.level.block.state.BlockState, CompoundTag> pair= blockStates[index.getX()][index.getY()][index.getZ()];
                    try {
                        BlockState blockState = pair.getLeft();
                        e.level.setBlock(offset, blockState, 11);

                        CompoundTag compoundTag = pair.getRight();

                        if (compoundTag != null) {
                            setTileEntity(e.level,offset,compoundTag);
                        }

                    } catch (NullPointerException ex) {
                        System.out.println("No block at "+ index);
                    }
                    progress++;
                } else {
                    System.out.println("finished");
                    blockStates = null;
                }
            }
        }
    }

    static boolean setTileEntity(Level world, BlockPos position, CompoundTag tag) {
        BlockEntity tileEntity = BlockEntity.loadStatic(position, world.getBlockState(position), tag);
        if (tileEntity == null) {
            return false;
        } else {
            world.setBlockEntity(tileEntity);
            return true;
        }
    }

    static Vec3i getIndex() {

        if(progress >= dimensions.getX() * dimensions.getY() * dimensions.getZ()) {
            return null;
        }

        int y = progress / (dimensions.getX() * dimensions.getZ());
        int xz = progress % (dimensions.getX() * dimensions.getZ());
        int z = xz / dimensions.getX();
        int x = xz % dimensions.getX();
        return new Vec3i(x,y,z);
    }
}
