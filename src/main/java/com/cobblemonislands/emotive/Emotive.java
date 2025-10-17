package com.cobblemonislands.emotive;

import com.cobblemonislands.emotive.command.EmotiveCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class Emotive implements ModInitializer {

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((commandDispatcher, commandBuildContext, commandSelection) -> {
            EmotiveCommand.register(commandDispatcher);
        });
    }
}
