package com.cobblemonislands.emotive.command;

import com.cobblemonislands.emotive.impl.GestureController;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Locale;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class EmotiveCommand {

    public static void register(com.mojang.brigadier.CommandDispatcher<CommandSourceStack> dispatcher) {
        List<String> animations = List.of("win", "wave");

        SuggestionProvider<CommandSourceStack> animationSuggestions = (context, builder) -> {
            String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
            for (String anim : animations) {
                if (anim.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                    builder.suggest(anim);
                }
            }
            return builder.buildFuture();
        };

        dispatcher.register(literal("emotive").then(argument("emote", StringArgumentType.word()).suggests(animationSuggestions).executes(ctx -> executeEmotive(ctx, animations))));
    }

    private static int executeEmotive(CommandContext<CommandSourceStack> ctx, List<String> animations) {
        CommandSourceStack source = ctx.getSource();

        if (!source.isPlayer()) {
            source.sendFailure(Component.literal("This command can only be run by a player!"));
            return 0;
        }

        ServerPlayer player = source.getPlayer();
        assert player != null;

        String animation = StringArgumentType.getString(ctx, "emote");

        if (!animations.contains(animation)) {
            source.sendFailure(Component.literal("Unknown emote: " + animation));
            return 0;
        }

        GestureController.onStart(player, animation);
        return Command.SINGLE_SUCCESS;
    }
}
