package com.cobblemonislands.emotive.config;

import com.cobblemonislands.emotive.storage.*;
import com.cobblemonislands.emotive.storage.hikari.MariaStorage;
import com.cobblemonislands.emotive.storage.hikari.PostgresStorage;
import com.cobblemonislands.emotive.storage.hikari.SqliteStorage;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

public class ModConfig {
    static Path CONFIG_FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve("emotive/config.json");
    static ModConfig instance;
    static Gson JSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(ResourceLocation.class, new SimpleCodecDeserializer<>(ResourceLocation.CODEC))
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
            .create();

    public boolean debug = false;

    public String command = "emotive";

    public boolean showPlayerName = true;

    @SerializedName("messages")
    public Messages messages = new Messages();

    public Map<String, Integer> permissions = Map.of(
            "emotive.command", 2,
            "emotive.give", 2,
            "emotive.remove", 2,
            "emotive.list", 2,
            "emotive.reload", 2,
            "emotive.direct", 2
    );


    public GuiConfig gui = new GuiConfig();

    transient EmoteStorage storage;

    public EmoteStorage.Type storageType = EmoteStorage.Type.MARIADB;
    public DatabaseConfig database = new DatabaseConfig.Builder()
            .host("localhost")
            .port(3306)
            .user("username")
            .password("secret")
            .maxPoolSize(10)
            .sslEnabled(false)
            .database("emotes_db")
            .build();

    public String mongoDbCollection = "emotes";

    public EmoteStorage getStorage() {
        if (storage != null) return storage;

        if (database == null) {
            this.storage = new LPStorage();
        } else {
            switch (storageType) {
                case MONGODB -> storage = new MongoCachedStorage(database, mongoDbCollection, 300);
                case MARIADB -> storage = new MariaStorage(database);
                case POSTGRESQL -> storage = new PostgresStorage(database);
                case SQLITE -> storage = new SqliteStorage(database);
                default -> storage = new LPStorage();
            }
        }

        return storage;
    }

    public @Nullable Pair<ResourceLocation, ConfiguredAnimation> getAnimation(String path) {
        for (Map.Entry<ResourceLocation, ConfiguredAnimation> entry : Animations.all().entrySet()) {
            if (entry.getKey().getPath().equals(path)) {
                return Pair.of(entry.getKey(), entry.getValue());
            }
        }

        return null;
    }

    public static ModConfig getInstance() {
        if (instance == null) {
            if (!load()) {
                save();
            }
        }
        return instance;
    }

    public static boolean load() {
        Animations.UNGROUPED.clear();
        Animations.GROUPED.clear();
        Categories.CATEGORIES.clear();

        CONFIG_FILE_PATH.toFile().getParentFile().mkdirs();

        if (!CONFIG_FILE_PATH.toFile().exists()) {
            instance = new ModConfig();

            Animations.saveExamples();
            Categories.saveExamples();

            try (FileOutputStream stream = new FileOutputStream(CONFIG_FILE_PATH.toFile())) {
                stream.write(JSON.toJson(instance).getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        }

        try {
            ModConfig.instance = JSON.fromJson(new FileReader(ModConfig.CONFIG_FILE_PATH.toFile()), ModConfig.class);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    private static void save() {
        try (FileOutputStream stream = new FileOutputStream(CONFIG_FILE_PATH.toFile())) {
            stream.write(JSON.toJson(instance).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
