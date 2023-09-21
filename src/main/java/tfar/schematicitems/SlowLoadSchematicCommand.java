package tfar.schematicitems;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.PlatformManager;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.forge.ForgeAdapter;
import com.sk89q.worldedit.forge.ForgePlayer;
import com.sk89q.worldedit.forge.WorldEditFakePlayer;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.io.Closer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import tfar.schematicitems.world.SlowPasteSavedData;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

public class SlowLoadSchematicCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("slowpaste")
                        .then(Commands.argument("schematic", StringArgumentType.string())
                                .then(Commands.argument("position", BlockPosArgument.blockPos())
                                        .executes(context -> slowloadSchematic(context, 256))
                                        .then(Commands.argument("speed", IntegerArgumentType.integer(1))
                                                .executes(SlowLoadSchematicCommand::slowloadSchematic)
                                        )
                                )
                        )
        );
    }

    public static int slowloadSchematic(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        int speed = IntegerArgumentType.getInteger(ctx, "speed");
        return slowloadSchematic(ctx, speed);
    }

    public static int slowloadSchematic(CommandContext<CommandSourceStack> ctx, int speed) throws CommandSyntaxException {
        String schematic = StringArgumentType.getString(ctx, "schematic");
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "position");
        ServerLevel level = ctx.getSource().getLevel();

        Clipboard clipboard = getSchematicForLoading(level,schematic);

        if (clipboard != null) {
            SlowPasteSavedData slowPasteSavedData = SlowPasteSavedData.loadFromLevel(level);
            slowPasteSavedData.addSlowPasteData(pos,clipboard,speed);
            ctx.getSource().sendSuccess(Component.literal("Schematic "+schematic+" successfully added to building queue"),true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.literal("Something went wrong while trying to load schematic "+schematic));
        return 0;
    }

    public static Clipboard getSchematicForLoading(ServerLevel level,String schematic) {

        String formatName = "sponge";

        WorldEdit worldEdit = WorldEdit.getInstance();
        LocalConfiguration config = worldEdit.getConfiguration();

        PlatformManager platformManager = worldEdit.getPlatformManager();
        ForgePlayer forgePlayer = ForgeAdapter.adaptPlayer(new WorldEditFakePlayer(level));
        Actor actor = platformManager.createProxyActor(forgePlayer);

        File dir = worldEdit.getWorkingDirectoryPath(config.saveDir).toFile();
        try {
            File f = worldEdit.getSafeOpenFile(actor, dir, schematic,
                    BuiltInClipboardFormat.SPONGE_SCHEMATIC.getPrimaryFileExtension(),
                    ClipboardFormats.getFileExtensionArray());

            ClipboardFormat format = ClipboardFormats.findByFile(f);
            if (format == null) {
                format = ClipboardFormats.findByAlias(formatName);
            }
            if (format == null) {
                actor.printError(TranslatableComponent.of("worldedit.schematic.unknown-format", TextComponent.of(formatName)));
                return null;
            }

            try (Closer closer = Closer.create()) {
                FileInputStream fis = closer.register(new FileInputStream(f));
                BufferedInputStream bis = closer.register(new BufferedInputStream(fis));
                ClipboardReader reader = closer.register(format.getReader(bis));

                return reader.read();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}

