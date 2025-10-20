package com.cobblemonislands.emotive;

import com.cobblemonislands.emotive.command.EmotiveCommand;
import com.cobblemonislands.emotive.component.ModComponents;
import com.cobblemonislands.emotive.config.Animations;
import com.cobblemonislands.emotive.config.Categories;
import com.cobblemonislands.emotive.config.ModConfig;
import com.cobblemonislands.emotive.impl.GestureController;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;

public class Emotive implements ModInitializer {
    public static final String MODID = "emotive";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        ModComponents.register();

        ModConfig.load();
        Animations.load();
        Categories.load();

        ServerPlayConnectionEvents.JOIN.register((serverGamePacketListener, packetSender, minecraftServer) -> GestureController.onConnect(serverGamePacketListener.player));
        ServerPlayConnectionEvents.DISCONNECT.register((serverGamePacketListener, minecraftServer) -> GestureController.onDisconnect(serverGamePacketListener.player));

        CommandRegistrationCallback.EVENT.register((commandDispatcher, commandBuildContext, commandSelection) -> EmotiveCommand.register(commandDispatcher));
    }
}
