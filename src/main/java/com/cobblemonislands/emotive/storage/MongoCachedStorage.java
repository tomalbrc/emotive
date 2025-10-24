package com.cobblemonislands.emotive.storage;

import com.cobblemonislands.emotive.Emotive;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MongoCachedStorage implements EmoteStorage {
    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final MongoCollection<Document> collection;
    private final LoadingCache<String, Map<String, Integer>> cache;

    public MongoCachedStorage(DatabaseConfig settings, String collectionName, long cacheExpireSeconds) {
        ConnectionString connString = new ConnectionString(settings.mongoConnectionString());

        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .applyToConnectionPoolSettings(builder ->
                        builder.maxSize(settings.maxPoolSize))
                .build();

        this.mongoClient = MongoClients.create(mongoClientSettings);
        this.database = mongoClient.getDatabase(settings.databaseName);
        this.collection = database.getCollection(collectionName);

        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(cacheExpireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<>() {
                    @Override
                    public @NotNull Map<String, Integer> load(@NotNull String uuid) {
                        return loadFromDb(uuid);
                    }
                });
    }

    public void close() {
        mongoClient.close();
    }

    private static long currentTs() {
        return System.currentTimeMillis() / 1000L;
    }

    private Map<String, Integer> loadFromDb(String uuid) {
        var cursor = collection.find(new Document("uuid", uuid));
        return cursor.into(new ObjectArrayList<>()).stream()
                .filter(doc -> doc.containsKey("key") && doc.containsKey("ts"))
                .collect(Collectors.toMap(
                        d -> d.getString("key"),
                        d -> {
                            Object o = d.get("ts");
                            if (o instanceof Number) {
                                return ((Number) o).intValue();
                            }
                            try {
                                return Integer.parseInt(o.toString());
                            } catch (NumberFormatException e) {
                                return 0;
                            }
                        }
                ));
    }

    public boolean add(ServerPlayer player, ResourceLocation animation) {
        String uuid = player.getUUID().toString();
        String key = String.format("%s.%s", Emotive.MODID, animation.toLanguageKey());

        try {
            Map<String, Integer> map = cache.get(uuid);
            if (map.containsKey(key)) {
                // already present
                return false;
            }
        } catch (ExecutionException ignored) {}

        long ts = currentTs();

        Document doc = new Document("uuid", uuid)
                .append("key", key)
                .append("ts", ts);
        collection.insertOne(doc);

        cache.invalidate(uuid);
        return true;
    }

    public boolean remove(ServerPlayer player, ResourceLocation animation) {
        String uuid = player.getUUID().toString();
        String key = String.format("%s.%s", Emotive.MODID, animation.toLanguageKey());

        var result = collection.deleteOne(new Document("uuid", uuid).append("key", key));
        boolean removed = result.getDeletedCount() > 0;
        if (removed) {
            cache.invalidate(uuid);
        }
        return removed;
    }

    public boolean owns(ServerPlayer player, ResourceLocation animation) {
        String uuid = player.getUUID().toString();
        String key = String.format("%s.%s", Emotive.MODID, animation.toLanguageKey());

        try {
            Map<String, Integer> map = cache.get(uuid);
            return map.containsKey(key);
        } catch (ExecutionException ex) {
            Document found = collection.find(
                    new Document("uuid", uuid).append("key", key)
            ).first();
            return found != null;
        }
    }

    public int timestamp(ServerPlayer player, ResourceLocation animation) {
        String uuid = player.getUUID().toString();
        String key = String.format("%s.%s", Emotive.MODID, animation.toLanguageKey());

        try {
            Map<String, Integer> map = cache.get(uuid);
            return map.getOrDefault(key, 0);
        } catch (ExecutionException ex) {
            Document found = collection.find(
                    new Document("uuid", uuid).append("key", key)
            ).first();
            if (found != null && found.containsKey("ts")) {
                Object o = found.get("ts");
                if (o instanceof Number) return ((Number) o).intValue();
                try {
                    return Integer.parseInt(o.toString());
                } catch (NumberFormatException ignored) {}
            }
            return 0;
        }
    }

    public List<String> list(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        try {
            Map<String, Integer> map = cache.get(uuid);
            return map.keySet().stream().filter(k -> k.startsWith(Emotive.MODID))
                    .collect(Collectors.toList());
        } catch (ExecutionException ex) {
            var cursor = collection.find(new Document("uuid", uuid));
            List<String> res = new ObjectArrayList<>();
            for (Document d : cursor) {
                String k = d.getString("key");
                if (k != null && k.startsWith(Emotive.MODID))
                    res.add(k);
            }
            return res;
        }
    }
}
