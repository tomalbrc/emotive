package com.cobblemonislands.emotive.storage;

import com.cobblemonislands.emotive.Emotive;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;

public class LPStorage implements EmoteStorage {

    private final LuckPerms luckPerms;

    public LPStorage() {
        this.luckPerms = LuckPermsProvider.get();
    }

    public LPStorage(LuckPerms luckPerms) {
        this.luckPerms = luckPerms;
    }

    public long timestamp() {
        return System.currentTimeMillis() / 1000L; // seconds
    }

    @Override
    public boolean add(ServerPlayer player, ResourceLocation animation) {
        User user = luckPerms.getUserManager().getUser(player.getUUID());
        String key = String.format("%s.%s", Emotive.MODID, animation.toLanguageKey());
        if (user != null && !owns(player, animation)) {
            MetaNode node = MetaNode.builder(key, Long.toString(timestamp())).build();
            user.data().clear(NodeType.META.predicate(mn -> mn.getMetaKey().equals(key)));
            user.data().add(node);
            luckPerms.getUserManager().saveUser(user);

            return true;
        }
        return false;
    }

    @Override
    public boolean remove(ServerPlayer player, ResourceLocation animation) {
        User user = luckPerms.getUserManager().getUser(player.getUUID());
        String key = String.format("%s.%s", Emotive.MODID, animation.toLanguageKey());
        if (user != null) {
            boolean[] removed = new boolean[]{false};
            user.data().clear(NodeType.META.predicate(mn -> {
                boolean eq = mn.getMetaKey().equals(key);
                if (eq)
                    removed[0] = true;
                return eq;
            }));
            luckPerms.getUserManager().saveUser(user);
            return removed[0];
        }
        return false;
    }

    @Override
    public boolean owns(ServerPlayer player, ResourceLocation animation) {
        User user = luckPerms.getUserManager().getUser(player.getUUID());
        if (user == null) return false;
        String key = String.format("%s.%s", Emotive.MODID, animation.toLanguageKey());
        return user.getCachedData().getMetaData().getMetaValue(key) != null;
    }

    @Override
    public List<String> list(ServerPlayer player) {
        List<String> result = new ObjectArrayList<>();
        User user = luckPerms.getUserManager().getUser(player.getUUID());
        if (user != null) {
            for (Map.Entry<String, List<String>> entry : user.getCachedData().getMetaData().getMeta().entrySet()) {
                if (entry.getKey().startsWith(Emotive.MODID)) {
                    result.add(entry.getKey().substring(Emotive.MODID.length()+1));
                }
            }
        }
        return result;
    }

    @Override
    public boolean addFav(ServerPlayer player, ResourceLocation emote) {
        if (!owns(player, emote)) return false;
        User user = luckPerms.getUserManager().getUser(player.getUUID());
        if (user == null) return false;

        String favKey = "fav." + Emotive.MODID + "." + emote.toLanguageKey();
        MetaNode node = MetaNode.builder(favKey, Long.toString(timestamp())).build();
        user.data().clear(NodeType.META.predicate(mn -> mn.getMetaKey().equals(favKey)));
        user.data().add(node);
        luckPerms.getUserManager().saveUser(user);
        return true;
    }

    @Override
    public boolean removeFav(ServerPlayer player, ResourceLocation emote) {
        User user = luckPerms.getUserManager().getUser(player.getUUID());
        if (user == null) return false;

        String favKey = "fav." + Emotive.MODID + "." + emote.toLanguageKey();
        boolean[] removed = new boolean[]{false};
        user.data().clear(NodeType.META.predicate(mn -> {
            boolean eq = mn.getMetaKey().equals(favKey);
            if (eq) removed[0] = true;
            return eq;
        }));
        luckPerms.getUserManager().saveUser(user);
        return removed[0];
    }

    @Override
    public List<String> listFavs(ServerPlayer player) {
        List<String> result = new ObjectArrayList<>();
        User user = luckPerms.getUserManager().getUser(player.getUUID());
        if (user != null) {
            for (Map.Entry<String, List<String>> entry :
                    user.getCachedData().getMetaData().getMeta().entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("fav.")) {
                    result.add(key.substring(4 + Emotive.MODID.length() + 1));
                }
            }
        }
        return result;
    }
}
