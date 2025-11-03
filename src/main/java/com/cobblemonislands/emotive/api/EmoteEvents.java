package com.cobblemonislands.emotive.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;

public interface EmoteEvents {
    /** Triggered when a player starts an emote */
    Event<StartEmote> START_EMOTE = EventFactory.createArrayBacked(StartEmote.class,
            (listeners) -> (player) -> {
                for (StartEmote listener : listeners) {
                    listener.onStartEmote(player);
                }
            });

    /** Triggered when a player stops an emote */
    Event<StopEmote> STOP_EMOTE = EventFactory.createArrayBacked(StopEmote.class,
            (listeners) -> (player) -> {
                for (StopEmote listener : listeners) {
                    listener.onStopEmote(player);
                }
            });

    @FunctionalInterface
    interface StartEmote {
        void onStartEmote(ServerPlayer player);
    }

    @FunctionalInterface
    interface StopEmote {
        void onStopEmote(ServerPlayer player);
    }
}