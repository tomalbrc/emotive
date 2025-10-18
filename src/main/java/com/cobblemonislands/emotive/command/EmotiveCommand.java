package com.cobblemonislands.emotive.command;

import com.cobblemonislands.emotive.config.ConfiguredAnimation;
import com.cobblemonislands.emotive.config.ModConfig;
import com.cobblemonislands.emotive.gui.EmoteSelectionGui;
import com.cobblemonislands.emotive.impl.GestureController;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class EmotiveCommand {
    public static void register(com.mojang.brigadier.CommandDispatcher<CommandSourceStack> dispatcher) {

        SuggestionProvider<CommandSourceStack> animationSuggestions = (context, builder) -> {
            String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
            for (Map.Entry<ResourceLocation, ConfiguredAnimation> anim : ModConfig.getInstance().animations.entrySet()) {
                var animation = anim.getValue();
                var id = anim.getKey();

                String animationKey = animation.permission(id);

                var player = context.getSource().getPlayer();

                if ((player == null || Permissions.check(player, animationKey, 2)) && animation.animationName().toLowerCase(Locale.ROOT).startsWith(remaining)) {
                    builder.suggest(id.getPath());
                }
            }
            return builder.buildFuture();
        };

        var rootPerm = Permissions.require("emotive.command", ModConfig.getInstance().permissions.getOrDefault("emotive.command", 2));

        var root = literal(ModConfig.getInstance().command).requires(rootPerm).executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayer();
            if (player != null) {
                EmoteSelectionGui gui = new EmoteSelectionGui(MenuType.GENERIC_9x6, player, false);
                gui.open();
                return Command.SINGLE_SUCCESS;
            }
            return 0;
        });

        root = root.then(literal("reload").requires(Permissions.require("emotive.reload")).executes(ctx -> {
            ModConfig.load();
            return Command.SINGLE_SUCCESS;
        }));

        dispatcher.register(root.then(argument("emote", StringArgumentType.word()).suggests(animationSuggestions).executes(EmotiveCommand::executeEmote)));
    }

    private static int executeEmote(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();

        if (!source.isPlayer()) {
            source.sendFailure(Component.literal("This command can only be run by a player!"));
            return 0;
        }

        ServerPlayer player = source.getPlayer();
        assert player != null;

        String animation = StringArgumentType.getString(ctx, "emote");

        if (ModConfig.getInstance().animations.values().stream().noneMatch(x -> x.animationName().equals(animation))) {
            source.sendFailure(Component.literal("Unknown emote: " + animation));
            return 0;
        }

        GestureController.onStart(player, animation);
        return Command.SINGLE_SUCCESS;
    }
}
