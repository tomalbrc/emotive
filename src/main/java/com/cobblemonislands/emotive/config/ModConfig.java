package com.cobblemonislands.emotive.config;

import com.cobblemonislands.emotive.configui.api.GuiData;
import com.cobblemonislands.emotive.configui.api.GuiElementData;
import com.cobblemonislands.emotive.storage.*;
import com.cobblemonislands.emotive.storage.hikari.MariaStorage;
import com.cobblemonislands.emotive.storage.hikari.PostgresStorage;
import com.cobblemonislands.emotive.storage.hikari.SqliteStorage;
import com.cobblemonislands.emotive.util.ItemStackDeserializer;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ModConfig {
    static Path CONFIG_FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve("emotive/config.json");
    static ModConfig instance;
    static Gson JSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(ItemStack.class, new ItemStackDeserializer())
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

    @SerializedName("fancy-hud")
    public boolean fancyHud = false;

    public Map<String, Integer> permissions = Map.of(
            "emotive.command", 2,
            "emotive.give", 2,
            "emotive.remove", 2,
            "emotive.list", 2,
            "emotive.reload", 2,
            "emotive.direct", 2
    );

    public GuiData<GuiElementData> selectionGui = new GuiData<>("Emote Selection", List.of(
            "B        ",
            " EEEEEEE ",
            "PEEEEEEEN",
            " EEEEEEE ",
            "         ",
            " FFFFFFF "
    ), Map.of(
            ' ', new GuiElementData("empty", null, Items.AIR.getDefaultInstance(), List.of(), List.of(), false),
            'N', new GuiElementData("next_page", "Next Page", Items.ARROW.getDefaultInstance(), List.of(), List.of(), false),
            'P', new GuiElementData("prev_page", "Previous Page", Items.ARROW.getDefaultInstance(), List.of(), List.of(), false),
            'B', new GuiElementData("browse", "Browse all Emotes", Items.CHEST.getDefaultInstance(), List.of(), List.of(), false),
            'E', new GuiElementData("emotes", null, Items.EMERALD.getDefaultInstance(), List.of("", "<gold>⌚</gold> Duration: <duration>", "<green>▶</green> Press <keybind:key.attack> to play", " ", "<color:#800080>↔</color> Press <keybind:key.use> to get as item", "", "<color:#800080>↔</color> Press <keybind:key.sneak> + <keybind:key.attack> to add to favourites"), List.of(), false),
            'F', new GuiElementData("favourites", null, Items.DIAMOND.getDefaultInstance(), List.of("<gold>⌚</gold> Duration: <duration>", "<green>▶</green> Press <keybind:key.attack> to play", "<color:#800080>↔</color> Press <keybind:key.sneak> + <keybind:key.attack> to remove from favourites"), List.of(), false)
    ), false);

    public GuiData<GuiElementData> browseGui = new GuiData<>("Browse Emotes", List.of(
            "B        ",
            " EEEEEEE ",
            "PEEEEEEEN",
            " EEEEEEE ",
            " EEEEEEE ",
            "         "
    ), Map.of(
            ' ', new GuiElementData("empty", null, Items.AIR.getDefaultInstance(), List.of(), List.of(), false),
            'N', new GuiElementData("next_page", "Next Page", Items.ARROW.getDefaultInstance(), List.of(), List.of(), false),
            'P', new GuiElementData("prev_page", "Previous Page", Items.ARROW.getDefaultInstance(), List.of(), List.of(), false),
            'B', new GuiElementData("back", "Back", Items.ARROW.getDefaultInstance(), List.of(), List.of(), false),
            'E', new GuiElementData("emotes", null, Items.EMERALD.getDefaultInstance(), List.of("", "<gold>You do not own this emote!"), List.of("", "<gold>⌚</gold> Duration: <duration>", "<green>You own this emote!"), false)
    ), false);

    public GuiData<GuiElementData> confirmationGui = new GuiData<>("Confirm", List.of(
            " C     A "
    ), Map.of(
            ' ', new GuiElementData("empty", null, Items.AIR.getDefaultInstance(), List.of(), List.of(), false),
            'A', new GuiElementData("confirm", "Confirm", Items.LIME_CONCRETE.getDefaultInstance(), List.of(), List.of(), false),
            'C', new GuiElementData("cancel", "Cancel", Items.RED_CONCRETE.getDefaultInstance(), List.of(), List.of(), false)
    ), false);

    private transient EmoteStorage storage;

    public EmoteStorage.Type storageType = EmoteStorage.Type.SQLITE;
    public DatabaseConfig database = new DatabaseConfig.Builder()
            .host("localhost")
            .port(3306)
            .user("username")
            .password("secret")
            .maxPoolSize(10)
            .sslEnabled(false)
            .database("cosmetic")
            .build();

    public String mongoDbCollection = "emotes";
    public String mongoDbCollectionFavourites = "emotes_favourites";

    public EmoteStorage getStorage() {
        if (storage != null) return storage;

        if (database == null) {
            this.storage = new LPStorage();
        } else {
            EmoteStorage emoteStorage;
            switch (storageType) {
                case MONGODB -> emoteStorage = new MongoCachedStorage(database, mongoDbCollection, mongoDbCollectionFavourites);
                case MARIADB -> emoteStorage = new MariaStorage(database);
                case POSTGRESQL -> emoteStorage = new PostgresStorage(database);
                case SQLITE -> emoteStorage = new SqliteStorage(database);
                default -> emoteStorage = new LPStorage();
            }
            this.storage = new CachedEmoteStorageProxy(emoteStorage);
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
