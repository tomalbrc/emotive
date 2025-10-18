package com.cobblemonislands.emotive.config;

import com.cobblemon.mod.common.api.npc.NPCClasses;
import com.cobblemonislands.emotive.impl.SimpleCodecDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModConfig {
    static Path CONFIG_FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve("emotive.json");
    static ModConfig instance;
    static Gson JSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(ResourceLocation.class, new SimpleCodecDeserializer<>(ResourceLocation.CODEC))
            .setPrettyPrinting()
            .create();

    @SerializedName("npc-class")
    public String npcClass = "standard";

    @SerializedName("messages")
    public Messages messages = new Messages();

    public String command = "emotive";

    public Map<ResourceLocation, ConfiguredAnimation> animations = Map.of(
            ResourceLocation.fromNamespaceAndPath("ci", "send_out"), new ConfiguredAnimation("Send Out", "send_out", 5, null, false),
            ResourceLocation.fromNamespaceAndPath("ci", "recall"), new ConfiguredAnimation("Recall", "recall", 5, null, false),
            ResourceLocation.fromNamespaceAndPath("ci", "lose"), new ConfiguredAnimation("Lose", "lose", 5, null, false),
            ResourceLocation.fromNamespaceAndPath("ci", "win"), new ConfiguredAnimation("Win", "win", 5, null, false)
    );

    public Map<String, Integer> permissions = Map.of(
            "emotive.command", 2
    );
    public boolean debug = false;

    public static ModConfig getInstance() {
        if (instance == null) {
            if (!load()) // only save if file wasn't just created
                save(); // save since newer versions may contain new options, also removes old options
        }
        return instance;
    }
    public static boolean load() {
        if (!CONFIG_FILE_PATH.toFile().exists()) {
            instance = new ModConfig();
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
