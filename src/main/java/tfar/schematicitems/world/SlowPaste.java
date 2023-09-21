package tfar.schematicitems.world;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.forge.ForgeAdapter;
import com.sk89q.worldedit.forge.internal.NBTConverter;
import com.sk89q.worldedit.internal.block.BlockStateIdAccess;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BaseBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;

public class SlowPaste {

    private int speed;
    private boolean noAir;
    private Pair<BlockState, CompoundTag>[][][] blocks;
    private Vec3i volume;
    private BlockPos start;
    private Vec3i origin;
    private int progress;

    public static SlowPaste create(BlockPos start, Clipboard clipboard, int speed,boolean noAir) {
        SlowPaste slowPaste = new SlowPaste();
        slowPaste.start = start;
        slowPaste.speed = speed;
        slowPaste.noAir = noAir;
        slowPaste.loadFromClipboard(clipboard);
        return slowPaste;
    }

    public void loadFromClipboard(Clipboard clipboard) {
        Region region = clipboard.getRegion();
        BlockVector3 blockVector3min = region.getMinimumPoint();
        BlockVector3 blockVector3max = region.getMaximumPoint();

        BlockVector3 schemOrigin = clipboard.getOrigin();

        origin = ForgeAdapter.toBlockPos(schemOrigin).subtract(ForgeAdapter.toBlockPos(blockVector3min));

        volume = ForgeAdapter.toBlockPos(clipboard.getDimensions());

        int x1 = blockVector3min.getX();
        int y1 = blockVector3min.getY();
        int z1 = blockVector3min.getZ();

        int x2 = blockVector3max.getX();
        int y2 = blockVector3max.getY();
        int z2 = blockVector3max.getZ();


        blocks = new Pair[volume.getX()][volume.getY()][volume.getZ()];

        for (int x = x1; x < x2 + 1; x++) {
            for (int y = y1; y < y2 + 1; y++) {
                for (int z = z1; z < z2 + 1; z++) {
                    BaseBlock worldEditBlockState = clipboard.getFullBlock(BlockVector3.at(x, y, z));

                    com.sk89q.jnbt.CompoundTag worldEditCompoundTag = worldEditBlockState.getNbtData();

                    int stateId = BlockStateIdAccess.getBlockStateId(worldEditBlockState.toImmutableState());
                    BlockState vanillaBlockState =
                            BlockStateIdAccess.isValidInternalId(stateId) ? Block.stateById(stateId) :
                                    ForgeAdapter.adapt(worldEditBlockState.toImmutableState());

                    //vanillaBlockState = Blocks.DIAMOND_BLOCK.defaultBlockState();worldEditCompoundTag = null;

                    int indexX = x - x1;
                    int indexY = y - y1;
                    int indexZ = z - z1;
                    blocks[indexX][indexY][indexZ] = Pair.of(vanillaBlockState, worldEditCompoundTag != null ? NBTConverter.toNative(worldEditCompoundTag) : null);
                }
            }
        }
    }

    public void tick(ServerLevel level) {
        for (int i = 0; i < speed; i++) {
            Vec3i index = getIndex();
            BlockPos offset = start.offset(index).subtract(origin);
            Pair<BlockState, CompoundTag> pair = blocks[index.getX()][index.getY()][index.getZ()];
            BlockState blockState = pair.getLeft();
            if (!blockState.isAir() || !noAir) {
                level.setBlock(offset, blockState, 11);
                CompoundTag compoundTag = pair.getRight();
                if (compoundTag != null) {
                    setTileEntity(level, offset, compoundTag);
                }
            }
            progress++;
            if (finished()) return;
        }
    }

    public boolean finished() {
        return progress >= volume.getX() * volume.getY() * volume.getZ();
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

    Vec3i getIndex() {
        int y = progress / (volume.getX() * volume.getZ());
        int xz = progress % (volume.getX() * volume.getZ());
        int z = xz / volume.getX();
        int x = xz % volume.getX();
        return new Vec3i(x, y, z);
    }

    public CompoundTag save() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putInt("speed", speed);
        compoundTag.putBoolean("noAir",noAir);
        compoundTag.put("blocks", saveBlocks());
        compoundTag.putIntArray("volume", new int[]{volume.getX(), volume.getY(), volume.getZ()});
        compoundTag.putIntArray("start", new int[]{start.getX(), start.getY(), start.getZ()});
        compoundTag.putIntArray("origin", new int[]{origin.getX(), origin.getY(), origin.getZ()});
        compoundTag.putInt("progress", progress);
        return compoundTag;
    }

    public static SlowPaste loadFromTag(CompoundTag tag) {
        SlowPaste slowPaste = new SlowPaste();
        slowPaste.speed = tag.getInt("speed");
        slowPaste.noAir = tag.getBoolean("noAir");
        int[] ints = tag.getIntArray("volume");
        slowPaste.volume = new Vec3i(ints[0], ints[1], ints[2]);
        int[] ints2 = tag.getIntArray("start");
        slowPaste.start = new BlockPos(ints2[0], ints2[1], ints2[2]);
        int[] ints3 = tag.getIntArray("origin");
        slowPaste.origin = new Vec3i(ints3[0], ints3[1], ints3[2]);
        slowPaste.loadBlocks(tag.getList("blocks", Tag.TAG_COMPOUND));
        slowPaste.progress = tag.getInt("progress");
        return slowPaste;
    }

    protected ListTag saveBlocks() {
        ListTag listTag = new ListTag();
        for (int x = 0; x < volume.getX(); x++) {
            for (int y = 0; y < volume.getY(); y++) {
                for (int z = 0; z < volume.getZ(); z++) {
                    CompoundTag compoundTag = new CompoundTag();
                    compoundTag.putIntArray("offset", new int[]{x, y, z});
                    Pair<BlockState, CompoundTag> pair = blocks[x][y][z];
                    compoundTag.put("state", NbtUtils.writeBlockState(pair.getLeft()));
                    CompoundTag beData = pair.getRight();
                    if (beData != null) {
                        compoundTag.put("tag", beData);
                    }
                    listTag.add(compoundTag);
                }
            }
        }
        return listTag;
    }

    protected void loadBlocks(ListTag tag) {
        blocks = new Pair[volume.getX()][volume.getY()][volume.getZ()];
        for (Tag iTag : tag) {
            CompoundTag compoundTag = (CompoundTag) iTag;
            int[] offset = compoundTag.getIntArray("offset");
            BlockState blockState = NbtUtils.readBlockState(compoundTag.getCompound("state"));
            CompoundTag beData = null;
            if (compoundTag.contains("tag")) {
                beData = compoundTag.getCompound("tag");
            }
            blocks[offset[0]][offset[1]][offset[2]] = Pair.of(blockState, beData);
        }
    }
}
