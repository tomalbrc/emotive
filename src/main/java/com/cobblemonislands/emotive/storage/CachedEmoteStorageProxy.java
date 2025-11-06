package com.cobblemonislands.emotive.storage;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class CachedEmoteStorageProxy implements EmoteStorage {
    private final EmoteStorage delegate;

    private final Cache<String, List<String>> emoteCache;
    private final Cache<String, List<String>> favCache;

    public CachedEmoteStorageProxy(EmoteStorage delegate) {
        this.delegate = delegate;

        this.emoteCache = CacheBuilder.newBuilder()
                .maximumSize(200)
                .build();

        this.favCache = CacheBuilder.newBuilder()
                .maximumSize(200)
                .build();
    }

    @Override
    public boolean add(ServerPlayer player, ResourceLocation emote) {
        boolean ok = delegate.add(player, emote);
        if (ok) {
            emoteCache.invalidate(player.getUUID().toString());
        }
        return ok;
    }

    @Override
    public boolean remove(ServerPlayer player, ResourceLocation emote) {
        boolean ok = delegate.remove(player, emote);
        if (ok) {
            emoteCache.invalidate(player.getUUID().toString());
        }
        return ok;
    }

    @Override
    public boolean addFav(ServerPlayer player, ResourceLocation emote) {
        boolean ok = delegate.addFav(player, emote);
        if (ok) {
            favCache.invalidate(player.getUUID().toString());
        }
        return ok;
    }

    @Override
    public boolean removeFav(ServerPlayer player, ResourceLocation emote) {
        boolean ok = delegate.removeFav(player, emote);
        if (ok) {
            favCache.invalidate(player.getUUID().toString());
        }
        return ok;
    }

    @Override
    public boolean owns(ServerPlayer player, ResourceLocation emote) {
        return list(player).contains(emote.toLanguageKey());
    }

    @Override
    public List<String> list(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        try {
            return emoteCache.get(uuid, () -> delegate.list(player));
        } catch (ExecutionException e) {
            return delegate.list(player);
        }
    }

    public List<String> listFavs(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        try {
            return favCache.get(uuid, () -> delegate.listFavs(player));
        } catch (ExecutionException e) {
            return delegate.listFavs(player);
        }
    }

    @Override
    public void close() {
        emoteCache.invalidateAll();
        favCache.invalidateAll();
        delegate.close();
    }

    @Override
    public void invalidate(ServerPlayer serverPlayer) {
        emoteCache.invalidate(serverPlayer.getUUID().toString());
        favCache.invalidate(serverPlayer.getUUID().toString());
    }
}
