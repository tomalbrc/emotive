package com.cobblemonislands.emotive.command;

import com.cobblemonislands.emotive.config.Animations;
import com.cobblemonislands.emotive.config.ConfiguredAnimation;
import com.cobblemonislands.emotive.config.ModConfig;
import com.cobblemonislands.emotive.gui.EmoteSelectionGui;
import com.cobblemonislands.emotive.impl.GestureController;
import com.cobblemonislands.emotive.util.TextUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.datafixers.util.Pair;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class EmotiveCommand {
    private EmotiveCommand() {}

    public static void register(com.mojang.brigadier.CommandDispatcher<CommandSourceStack> dispatcher) {
        final ModConfig config = ModConfig.getInstance();

        SuggestionProvider<CommandSourceStack> animationSuggestions =  (context, builder) -> {
            final String rem = builder.getRemaining().toLowerCase(Locale.ROOT);

            for (Map.Entry<ResourceLocation, ConfiguredAnimation> entry : Animations.all().entrySet()) {
                final ResourceLocation id = entry.getKey();
                final ConfiguredAnimation animation = entry.getValue();

                final ServerPlayer sourcePlayer = context.getSource().getPlayer();
                final boolean hasAccess = (sourcePlayer == null)
                        || sourcePlayer.hasPermissions(4)
                        || ModConfig.getInstance().getStorage().owns(sourcePlayer, id);

                if (!hasAccess) continue;

                final String nameLower = animation.animationName().toLowerCase(Locale.ROOT);
                final String idPath = id.getPath().toLowerCase(Locale.ROOT);

                if (nameLower.startsWith(rem) || idPath.startsWith(rem)) {
                    builder.suggest(id.getPath());
                }
            }

            return builder.buildFuture();
        };

        final Function<String, Predicate<CommandSourceStack>> requirePerm = (permKey) ->
                Permissions.require(permKey, config.permissions.getOrDefault(permKey, 2));

        var root = literal(config.command)
                .requires(requirePerm.apply("emotive.command"))
                .executes(EmotiveCommand::openGui);

        // /<cmd> reload
        root = root.then(literal("reload")
                .requires(requirePerm.apply("emotive.reload"))
                .executes(ctx -> {
                    ModConfig.load();
                    ctx.getSource().sendSuccess(() -> TextUtil.parse(ModConfig.getInstance().messages.configReloaded), false);
                    return Command.SINGLE_SUCCESS;
                }));

        // /<cmd> give <player> <emote>
        root = root.then(literal("give")
                .requires(requirePerm.apply("emotive.give"))
                .then(argument("player", EntityArgument.player())
                        .then(literal("*").executes(EmotiveCommand::handleAddAll))
                        .then(argument("emote", StringArgumentType.word()).suggests(animationSuggestions)
                                .executes(EmotiveCommand::handleAdd))));

        // /<cmd> remove <player> <emote>
        root = root.then(literal("remove")
                .requires(requirePerm.apply("emotive.remove"))
                .then(argument("player", EntityArgument.player())
                        .then(literal("*").executes(EmotiveCommand::handleRemoveAll))
                        .then(argument("emote", StringArgumentType.word()).suggests(animationSuggestions)
                                .executes(EmotiveCommand::handleRemove))));

        // /<cmd> list <player>
        root = root.then(literal("list")
                .requires(requirePerm.apply("emotive.list"))
                .then(argument("player", EntityArgument.player())
                        .executes(EmotiveCommand::handleList)));

        // run emote directly: /<cmd> <emote>
        dispatcher.register(root.then(argument("emote", StringArgumentType.word())
                .requires(requirePerm.apply("emotive.direct"))
                .suggests(animationSuggestions)
                .executes(EmotiveCommand::executeEmote)));
    }

    private static int openGui(CommandContext<CommandSourceStack> ctx) {
        final ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;

        final EmoteSelectionGui gui = new EmoteSelectionGui(player, false);
        gui.open();
        return Command.SINGLE_SUCCESS;
    }

    private static int handleAddAll(CommandContext<CommandSourceStack> ctx) {
        try {
            final ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
            int added = (int) Animations.all().keySet().stream().filter(name -> ModConfig.getInstance().getStorage().add(player, name)).count();
            ctx.getSource().sendSuccess(() -> Component.literal(String.format("Successfully added %d emotes to %s", added, player.getScoreboardName())), false);
        } catch (CommandSyntaxException e) {
            ctx.getSource().sendFailure(TextUtil.parse(ModConfig.getInstance().messages.playerNotFound));
            return 0;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("An unexpected error occurred while adding all emote"));
            return 0;
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int handleRemoveAll(CommandContext<CommandSourceStack> ctx) {
        try {
            final ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
            int added = (int) Animations.all().keySet().stream().filter(name -> ModConfig.getInstance().getStorage().remove(player, name)).count();
            ctx.getSource().sendSuccess(() -> Component.literal(String.format("Successfully removed %d emotes from %s", added, player.getScoreboardName())), false);
        } catch (CommandSyntaxException e) {
            ctx.getSource().sendFailure(TextUtil.parse(ModConfig.getInstance().messages.playerNotFound));
            return 0;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("An unexpected error occurred while removing all emote"));
            return 0;
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int handleAdd(CommandContext<CommandSourceStack> ctx) {
        try {
            final ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
            final String emoteName = StringArgumentType.getString(ctx, "emote");
            final var emote = ModConfig.getInstance().getAnimation(emoteName);

            final boolean success = emote != null && ModConfig.getInstance().getStorage().add(player, emote.getFirst());
            if (success) {
                ctx.getSource().sendSuccess(() -> Component.literal(String.format("Successfully added '%s' to %s", emote, player.getScoreboardName())), false);
                return Command.SINGLE_SUCCESS;
            } else {
                ctx.getSource().sendFailure(Component.literal(String.format("%s already owns '%s'", player.getScoreboardName(), emote)));
                return 0;
            }
        } catch (CommandSyntaxException e) {
            ctx.getSource().sendFailure(TextUtil.parse(ModConfig.getInstance().messages.playerNotFound));
            return 0;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("An unexpected error occurred while adding emote"));
            return 0;
        }
    }

    private static int handleRemove(CommandContext<CommandSourceStack> ctx) {
        try {
            final ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
            final String emote = StringArgumentType.getString(ctx, "emote");
            final Pair<ResourceLocation, ConfiguredAnimation> res = ModConfig.getInstance().getAnimation(emote);
            final boolean success = res != null && ModConfig.getInstance().getStorage().remove(player, res.getFirst());
            if (success) {
                ctx.getSource().sendSuccess(() -> Component.literal(String.format("Successfully removed '%s' from %s", emote, player.getScoreboardName())), false);
                return Command.SINGLE_SUCCESS;
            } else {
                ctx.getSource().sendFailure(Component.literal(String.format("%s does not own '%s'", player.getScoreboardName(), emote)));
                return 0;
            }
        } catch (CommandSyntaxException e) {
            ctx.getSource().sendFailure(TextUtil.parse(ModConfig.getInstance().messages.playerNotFound));
            return 0;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("An unexpected error occurred while removing emote"));
            return 0;
        }
    }

    private static int handleList(CommandContext<CommandSourceStack> ctx) {
        try {
            final ServerPlayer player = EntityArgument.getPlayer(ctx, "player");

            CompletableFuture.runAsync(() -> {
                final List<String> list = ModConfig.getInstance().getStorage().list(player);
                if (list.isEmpty()) {
                    ctx.getSource().sendFailure(Component.literal(String.format("No entries for player %s!", player.getScoreboardName())));
                    return;
                }

                for (String s : list) {
                    ctx.getSource().sendSuccess(() -> Component.literal(s), false);
                }
            });

            return Command.SINGLE_SUCCESS;
        } catch (CommandSyntaxException e) {
            ctx.getSource().sendFailure(TextUtil.parse(ModConfig.getInstance().messages.playerNotFound));
            return 0;
        }
    }

    private static int executeEmote(CommandContext<CommandSourceStack> ctx) {
        final CommandSourceStack source = ctx.getSource();

        if (!source.isPlayer()) {
            source.sendFailure(TextUtil.parse(ModConfig.getInstance().messages.onlyPlayer));
            return 0;
        }

        final ServerPlayer player = source.getPlayer();
        if (player == null) return 0;

        final String animation = StringArgumentType.getString(ctx, "emote");
        final var anim = ModConfig.getInstance().getAnimation(animation);
        CompletableFuture.runAsync(() -> {
            if (anim == null || !ModConfig.getInstance().getStorage().owns(player, anim.getFirst())) {
                source.sendFailure(TextUtil.parse(ModConfig.getInstance().messages.noPermission));
                return;
            }

            final ConfiguredAnimation found = Animations.all().values().stream()
                    .filter(a -> a.animationName().equals(animation))
                    .findFirst().orElse(null);

            if (found == null) {
                if (ModConfig.getInstance().messages.unknownEmote != null) source.sendFailure(TextUtil.parse(String.format(ModConfig.getInstance().messages.unknownEmote, animation)));
                return;
            }

            ctx.getSource().getServer().execute(() -> GestureController.onStart(player, found));
        });

        return Command.SINGLE_SUCCESS;
    }
}
