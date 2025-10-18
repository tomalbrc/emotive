package com.cobblemonislands.emotive;

import com.cobblemonislands.emotive.command.EmotiveCommand;
import com.cobblemonislands.emotive.impl.GestureController;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class Emotive implements ModInitializer {

    public static final String MODID = "emotive";

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.JOIN.register((serverGamePacketListener, packetSender, minecraftServer) -> GestureController.onConnect(serverGamePacketListener.player));
        ServerPlayConnectionEvents.DISCONNECT.register((serverGamePacketListener, minecraftServer) -> GestureController.onDisconnect(serverGamePacketListener.player));

        CommandRegistrationCallback.EVENT.register((commandDispatcher, commandBuildContext, commandSelection) -> EmotiveCommand.register(commandDispatcher));
    }
}
