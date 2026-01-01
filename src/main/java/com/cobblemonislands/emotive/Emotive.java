package com.cobblemonislands.emotive;

import com.cobblemonislands.emotive.command.EmotiveCommand;
import com.cobblemonislands.emotive.component.ModComponents;
import com.cobblemonislands.emotive.config.Animations;
import com.cobblemonislands.emotive.config.Categories;
import com.cobblemonislands.emotive.config.ModConfig;
import com.cobblemonislands.emotive.hud.FlatHudHolder;
import com.cobblemonislands.emotive.impl.GestureController;
import com.cobblemonislands.emotive.impl.PlayerModelRegistry;
import com.cobblemonislands.emotive.newpolymer.BitmapProvider;
import com.cobblemonislands.emotive.newpolymer.FontAsset;
import com.cobblemonislands.emotive.util.EmoteFont;
import com.cobblemonislands.emotive.util.Util;
import com.mojang.logging.LogUtils;
import eu.pb4.polymer.resourcepack.api.AssetPaths;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.util.StringUtil;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Emotive implements ModInitializer {
    public static final String MODID = "emotive";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        ModComponents.register();

        ModConfig.load();
        Animations.load();
        Categories.load();
        PlayerModelRegistry.load();

        PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(EmoteFont::createFont);

        ServerPlayConnectionEvents.DISCONNECT.register((serverGamePacketListener, minecraftServer) -> {
            GestureController.onDisconnect(serverGamePacketListener.player);
            ModConfig.getInstance().getStorage().invalidate(serverGamePacketListener.player);
        });

        CommandRegistrationCallback.EVENT.register((commandDispatcher, commandBuildContext, commandSelection) -> EmotiveCommand.register(commandDispatcher));

        ServerLifecycleEvents.SERVER_STOPPING.register(x -> ModConfig.getInstance().getStorage().close());
    }

    public static void openHud(ServerPlayer player) {
        if (!GestureController.GESTURE_CAMS.containsKey(player.getUUID()) && !player.hasContainerOpen()) {
            var hud = new FlatHudHolder(player, PlayerModelRegistry.getAnimations().subList(0, 9));
            EntityAttachment.ofTicking(hud, player);
            hud.startWatching(player);
        }
    }


}
