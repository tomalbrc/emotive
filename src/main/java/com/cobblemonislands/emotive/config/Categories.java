package com.cobblemonislands.emotive.config;

import com.cobblemonislands.emotive.Emotive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
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

public class Categories {
    public static Map<String, ConfiguredCategory> CATEGORIES = new Object2ObjectArrayMap<>();

    public static void add(String id, ConfiguredCategory category) {
        CATEGORIES.put(id, category);
        for (var entry : category.animations().entrySet()) {
            Animations.addGrouped(entry.getKey(), entry.getValue());
        }
    }

    public static void load() {
        Path dir = FabricLoader.getInstance().getConfigDir().resolve("emotive/categories");
        dir.toFile().mkdirs();

        if (Files.exists(dir) && Files.isDirectory(dir)) {
            try (var list = Files.list(dir)) {
                list.filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                    try (Reader reader = Files.newBufferedReader(path)) {
                        JsonReader jsonReader = new JsonReader(reader);
                        ConfiguredCategory category = ModConfig.JSON.fromJson(jsonReader, ConfiguredCategory.class);

                        if (category == null) {
                            return;
                        }

                        String id = category.id();
                        if (id == null || id.isBlank()) {
                            Emotive.LOGGER.error("Warning: category in {} has no id; skipping", path);
                            return;
                        }

                        add(id, category);
                    } catch (JsonSyntaxException e) {
                        Emotive.LOGGER.error("JSON syntax error in file {}: {}", path, e.getMessage());
                    } catch (IOException e) {
                        Emotive.LOGGER.error("I/O error reading file {}: {}", path, e.getMessage());
                    }
                });
            } catch (IOException e) {
                Emotive.LOGGER.error("I/O error listing directory {}: {}", dir, e.getMessage());
            }
        } else {
            Emotive.LOGGER.error("Categories directory not found: {}", dir);
        }
    }

    public static void saveExamples() {
        Map<ResourceLocation, ConfiguredAnimation> animations = Map.of(
                ResourceLocation.fromNamespaceAndPath("cobblemon", "lose"),
                ConfiguredAnimation.builder()
                        .title("<rainbow>Lose")
                        .animationName("lose")
                        .duration(5)
                        .item(Items.CHARCOAL.builtInRegistryHolder().key().location())
                        .build(),
                ResourceLocation.fromNamespaceAndPath("cobblemon", "win"),
                ConfiguredAnimation.builder()
                        .title("<rainbow>Win")
                        .npcClass(ResourceLocation.fromNamespaceAndPath("cobblemon", "standard"))
                        .animationName("win")
                        .duration(5)
                        .item(Items.CHARCOAL.builtInRegistryHolder().key().location())
                        .build()
        );

        ConfiguredCategory category = new ConfiguredCategory("example_category", "<rainbow>Example Category</rainbow>", Items.KNOWLEDGE_BOOK.builtInRegistryHolder().key().location(), null, null, false, animations);

        Path dir = FabricLoader.getInstance().getConfigDir().resolve("emotive/categories");
        dir.toFile().mkdirs();
        var filepath = dir.resolve("example.json");
        try (FileOutputStream stream = new FileOutputStream(filepath.toFile())) {
            stream.write(ModConfig.JSON.toJson(category).getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignored) {}
    }
}
