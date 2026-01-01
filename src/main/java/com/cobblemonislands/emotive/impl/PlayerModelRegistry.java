package com.cobblemonislands.emotive.impl;

import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.file.loader.AjBlueprintLoader;
import de.tomalbrc.bil.file.loader.AjModelLoader;
import de.tomalbrc.bil.file.loader.BbModelLoader;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.FilenameUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class PlayerModelRegistry {
    static Map<String, Model> texturedModelMap = new Object2ObjectArrayMap<>();

    public static void load() {
        var path = FabricLoader.getInstance().getConfigDir().resolve("emotive/models/");
        try {
            Files.walk(path).forEach(x -> {
                if (x.toString().endsWith(".bbmodel")) {
                    try {
                        Model normalModel = new BbModelLoader().load(new FileInputStream(x.toFile()), FilenameUtils.getBaseName(x.toString()));
                        for (String animationName : normalModel.animations().keySet()) {
                            texturedModelMap.put(animationName, normalModel);
                        }
                    } catch (FileNotFoundException e) {}
                }

                if (x.toString().endsWith(".ajblueprint")) {
                    try {
                        Model normalModel = new AjBlueprintLoader().load(new FileInputStream(x.toFile()), FilenameUtils.getBaseName(x.toString()));
                        for (String animationName : normalModel.animations().keySet()) {
                            texturedModelMap.put(animationName, normalModel);
                        }
                    } catch (FileNotFoundException e) {}
                }

                if (x.toString().endsWith(".ajmodel")) {
                    try {
                        Model normalModel = new AjModelLoader().load(new FileInputStream(x.toFile()), FilenameUtils.getBaseName(x.toString()));
                        for (String animationName : normalModel.animations().keySet()) {
                            texturedModelMap.put(animationName, normalModel);
                        }
                    } catch (FileNotFoundException e) {}
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static Model getTexturedModel(String animationName) {
        return texturedModelMap.get(animationName);
    }

    public static Model getModel(String animationName) {
        return texturedModelMap.get(animationName);
    }

    public static List<String> getAnimations() {
        return texturedModelMap.keySet().stream().toList();
    }
}
