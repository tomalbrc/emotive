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

public class LPStorage {
    public static LuckPerms LP = LuckPermsProvider.get();

    public static long timestamp() {
        return System.currentTimeMillis() / 1000L; // seconds
    }

    public static boolean add(ServerPlayer player, ResourceLocation animation) {
        User user = LP.getUserManager().getUser(player.getUUID());
        String key = String.format("%s.%s", Emotive.MODID, animation.toLanguageKey());
        if (user != null && !owns(player, animation)) {
            MetaNode node = MetaNode.builder(key, Long.toString(timestamp())).build();
            user.data().clear(NodeType.META.predicate(mn -> mn.getMetaKey().equals(key)));
            user.data().add(node);
            LP.getUserManager().saveUser(user);

            return true;
        }

        return false;
    }

    public static boolean remove(ServerPlayer player, ResourceLocation animation) {
        User user = LP.getUserManager().getUser(player.getUUID());
        String key = String.format("%s.%s", Emotive.MODID, animation.toLanguageKey());
        if (user != null) {
            boolean[] locked = new boolean[]{false};
            user.data().clear(NodeType.META.predicate(mn -> {
                var eq = mn.getMetaKey().equals(key);
                if (eq)
                    locked[0] = true;

                return mn.getMetaKey().equals(key);
            }));
            LP.getUserManager().saveUser(user);

            return locked[0];
        }

        return false;
    }

    public static boolean owns(ServerPlayer player, ResourceLocation animation) {
        User user = LP.getUserManager().getUser(player.getUUID());
        String key = String.format("%s.%s", Emotive.MODID, animation.toLanguageKey());
        return user != null && user.getCachedData().getMetaData().getMetaValue(key) != null;
    }

    public static List<String> list(ServerPlayer player) {
        List<String> res = new ObjectArrayList<>();
        User user = LP.getUserManager().getUser(player.getUUID());
        if (user != null) {
            for (Map.Entry<String, List<String>> entry : user.getCachedData().getMetaData().getMeta().entrySet()) {
                if (entry.getKey().startsWith(Emotive.MODID)) res.add(entry.getKey());

            }
        }
        return res;
    }
}
