package com.cobblemonislands.emotive.config;

import com.cobblemonislands.emotive.Emotive;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class Animations {
    public static Map<ResourceLocation, ConfiguredAnimation> GROUPED = new Object2ObjectArrayMap<>();
    public static Map<ResourceLocation, ConfiguredAnimation> UNGROUPED = new Object2ObjectArrayMap<>();

    public static void addGrouped(ResourceLocation id, ConfiguredAnimation animation) {
        GROUPED.put(id, animation);
    }

    public static void addUngrouped(ResourceLocation id, ConfiguredAnimation animation) {
        UNGROUPED.put(id, animation);
    }

    public static Object2ObjectOpenHashMap<ResourceLocation, ConfiguredAnimation> all() {
        Object2ObjectOpenHashMap<ResourceLocation, ConfiguredAnimation> map = new Object2ObjectOpenHashMap<>();
        map.putAll(GROUPED);
        map.putAll(UNGROUPED);
        return map;
    }

    public static void load() {
        Path dir = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("emotive/animations");

        dir.toFile().mkdirs();

        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            Emotive.LOGGER.error("Animations directory not found: {}", dir);
            return;
        }

        try (var files = Files.list(dir)) {
            files
                    .filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        try (Reader reader = Files.newBufferedReader(path)) {
                            JsonReader jsonReader = new JsonReader(reader);
                            JsonObject root = ModConfig.JSON.fromJson(jsonReader, JsonObject.class);

                            if (root == null || !root.has("animations")) {
                                Emotive.LOGGER.warn("File {} does not contain 'animations' object; skipping", path);
                                return;
                            }

                            JsonObject animationsObj = root.getAsJsonObject("animations");
                            for (Map.Entry<String, JsonElement> entry : animationsObj.entrySet()) {
                                String key = entry.getKey();
                                JsonElement value = entry.getValue();

                                try {
                                    ConfiguredAnimation animation = ModConfig.JSON.fromJson(value, ConfiguredAnimation.class);
                                    ResourceLocation id = ResourceLocation.parse(key);
                                    addUngrouped(id, animation);
                                } catch (Exception e) {
                                    Emotive.LOGGER.error("Failed to parse animation '{}' in file {}: {}", key, path, e.getMessage());
                                }
                            }

                        } catch (JsonSyntaxException e) {
                            Emotive.LOGGER.error("JSON syntax error in file {}: {}", path, e.getMessage());
                        } catch (IOException e) {
                            Emotive.LOGGER.error("I/O error reading file {}: {}", path, e.getMessage());
                        }
                    });
        } catch (IOException e) {
            Emotive.LOGGER.error("I/O error listing directory {}: {}", dir, e.getMessage());
        }
    }

    public static void saveExamples() {
        Map<ResourceLocation, ConfiguredAnimation> animations = Map.of(
                ResourceLocation.fromNamespaceAndPath("cobblemon", "send_out"),
                ConfiguredAnimation.builder()
                        .title("<rainbow>Send Out")
                        .animationName("send_out")
                        .duration(5)
                        .item(Items.SLIME_BALL.builtInRegistryHolder().key().location())
                        .build(),
                ResourceLocation.fromNamespaceAndPath("cobblemon", "recall"),
                ConfiguredAnimation.builder()
                        .title("<rainbow>Recall")
                        .animationName("recall")
                        .duration(5)
                        .item(Items.EMERALD_BLOCK.builtInRegistryHolder().key().location())
                        .glint(true)
                        .build(),
                ResourceLocation.fromNamespaceAndPath("cobblemon", "punch_left"),
                ConfiguredAnimation.builder()
                        .title("<rainbow>Punch Left")
                        .npcClass(ResourceLocation.fromNamespaceAndPath("cobblemon", "standard"))
                        .animationName("punch_left")
                        .duration(5)
                        .item(Items.CHARCOAL.builtInRegistryHolder().key().location())
                        .build()
        );

        JsonObject object = new JsonObject();
        object.add("animations", ModConfig.JSON.toJsonTree(animations));
        Path dir = FabricLoader.getInstance().getConfigDir().resolve("emotive/animations");
        dir.toFile().mkdirs();
        var filepath = dir.resolve("examples.json");
        try (FileOutputStream stream = new FileOutputStream(filepath.toFile())) {
            stream.write(ModConfig.JSON.toJson(object).getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignored) {}
    }
}
