package com.cobblemonislands.emotive.storage;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * Generic interface for storing and retrieving player emote data.
 */
public interface EmoteStorage {

    enum Type {
        MARIADB,
        POSTGRESQL,
        SQLITE,
        MONGODB,
        LPMETA
    }

    /**
     * Adds a new emote to a player’s storage.
     * @return true if successfully added, false otherwise.
     */
    boolean add(ServerPlayer player, ResourceLocation emote);

    /**
     * Removes an emote from the player’s storage.
     * @return true if successfully removed, false otherwise.
     */
    boolean remove(ServerPlayer player, ResourceLocation emote);

    boolean addFav(ServerPlayer player, ResourceLocation emote);

    boolean removeFav(ServerPlayer player, ResourceLocation element);

    /**
     * Checks if the player owns the specified emote.
     */
    boolean owns(ServerPlayer player, ResourceLocation emote);

    /**
     * Lists all stored emote keys for the player.
     */
    List<String> list(ServerPlayer player);

    List<String> listFavs(ServerPlayer player);

    default void close() {}

    default void invalidate(ServerPlayer serverPlayer) {}
}
