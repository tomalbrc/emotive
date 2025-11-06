package com.cobblemonislands.emotive.storage;

import com.cobblemonislands.emotive.Emotive;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.bson.Document;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MongoCachedStorage implements EmoteStorage {
    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final MongoCollection<Document> ownsCollection;
    private final MongoCollection<Document> favsCollection;

    public MongoCachedStorage(DatabaseConfig settings,
                              String ownsCollectionName,
                              String favsCollectionName) {
        ConnectionString connString = new ConnectionString(settings.mongoConnectionString());

        this.mongoClient = MongoClients.create(connString);
        this.database = mongoClient.getDatabase(settings.databaseName);
        this.ownsCollection = database.getCollection(ownsCollectionName);
        this.favsCollection = database.getCollection(favsCollectionName);
    }

    public void close() {
        mongoClient.close();
    }

    private static long currentTs() {
        return System.currentTimeMillis() / 1000L;
    }

    private Map<String, Integer> loadOwnsFromDb(String uuid) {
        return ownsCollection
                .find(new Document("uuid", uuid))
                .into(new java.util.ArrayList<>())
                .stream()
                .filter(d -> d.containsKey("key") && d.containsKey("ts"))
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

    private Map<String, Integer> loadFavsFromDb(String uuid) {
        return favsCollection
                .find(new Document("uuid", uuid))
                .into(new java.util.ArrayList<>())
                .stream()
                .filter(d -> d.containsKey("key") && d.containsKey("ts"))
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

    @Override
    public boolean add(ServerPlayer player, ResourceLocation animation) {
        String uuid = player.getUUID().toString();
        String key = String.format("%s.%s", Emotive.MODID, animation.toLanguageKey());

        Map<String, Integer> map = loadOwnsFromDb(uuid);
        if (map.containsKey(key)) {
            return false;
        }

        long ts = currentTs();

        Document doc = new Document("uuid", uuid)
                .append("key", key)
                .append("ts", ts);
        ownsCollection.insertOne(doc);
        return true;
    }

    @Override
    public boolean remove(ServerPlayer player, ResourceLocation animation) {
        String uuid = player.getUUID().toString();
        String key = String.format("%s.%s", Emotive.MODID, animation.toLanguageKey());

        var result = ownsCollection.deleteOne(
                new Document("uuid", uuid).append("key", key)
        );
        return result.getDeletedCount() > 0;
    }

    @Override
    public boolean owns(ServerPlayer player, ResourceLocation animation) {
        String uuid = player.getUUID().toString();
        String key = String.format("%s.%s", Emotive.MODID, animation.toLanguageKey());

        Map<String, Integer> map = loadOwnsFromDb(uuid);
        return map.containsKey(key);
    }

    @Override
    public List<String> list(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        Map<String, Integer> map = loadOwnsFromDb(uuid);
        return map.keySet().stream()
                .filter(k -> k.startsWith(Emotive.MODID + "."))
                .map(x -> x.substring(Emotive.MODID.length()+1))
                .collect(Collectors.toList());
    }

    @Override
    public boolean addFav(ServerPlayer player, ResourceLocation emote) {
        String uuid = player.getUUID().toString();
        String key = String.format("%s.%s", Emotive.MODID, emote.toLanguageKey());

        Map<String, Integer> favMap = loadFavsFromDb(uuid);
        if (favMap.containsKey(key)) {
            return false;
        }

        long ts = currentTs();

        Document doc = new Document("uuid", uuid)
                .append("key", key)
                .append("ts", ts);
        favsCollection.insertOne(doc);
        return true;
    }

    @Override
    public boolean removeFav(ServerPlayer player, ResourceLocation element) {
        String uuid = player.getUUID().toString();
        String key = String.format("%s.%s", Emotive.MODID, element.toLanguageKey());

        var result = favsCollection.deleteOne(new Document("uuid", uuid).append("key", key));
        return result.getDeletedCount() > 0;
    }

    @Override
    public List<String> listFavs(ServerPlayer player) {
        String uuid = player.getUUID().toString();
        Map<String, Integer> map = loadFavsFromDb(uuid);
        return map.keySet().stream()
                .filter(k -> k.startsWith(Emotive.MODID + "."))
                .map(x -> x.substring(Emotive.MODID.length()+1))
                .collect(Collectors.toList());
    }
}
