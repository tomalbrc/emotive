package com.cobblemonislands.emotive.util;

import com.cobblemonislands.emotive.Emotive;
import com.cobblemonislands.emotive.config.ModConfig;
import com.cobblemonislands.emotive.impl.PlayerModelRegistry;
import com.cobblemonislands.emotive.newpolymer.BitmapProvider;
import com.cobblemonislands.emotive.newpolymer.FontAsset;
import eu.pb4.polymer.resourcepack.api.AssetPaths;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EmoteFont {
    static byte[] DEFAULT_ICON;
    static byte[] BLANK_ICON;

    public static final Map<String, String> ANIMATION_ICONS = new HashMap<>();

    static {
        try {
            DEFAULT_ICON = Objects.requireNonNull(Emotive.class.getResourceAsStream("/default-emote-icon.png")).readAllBytes();
            BLANK_ICON = Objects.requireNonNull(Emotive.class.getResourceAsStream("/blank-emote-icon.png")).readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createFont(ResourcePackBuilder resourcePackBuilder) {
        if (!ModConfig.getInstance().fancyHud)
            return;

        var fontAssetBuilder = FontAsset.builder();
        fontAssetBuilder.add(BitmapProvider.builder(Util.id("font/blank-emote-icon")).height(32).ascent(16).chars(String.valueOf((char) (0xE200))));
        resourcePackBuilder.addData(AssetPaths.texture(Util.id("font/blank-emote-icon")), BLANK_ICON);

        var animations = PlayerModelRegistry.getAnimations();
        for (int i = 0; i < animations.size(); i++) {
            var animation = animations.get(i);
            var name = animation + "_icon";
            var iconStream = getIcon(name);
            var entry = String.valueOf((char) (0xE001 + i));
            if (iconStream != null) {
                fontAssetBuilder.add(BitmapProvider.builder(Util.id("font/" + name)).height(32).ascent(16).chars(entry));
                resourcePackBuilder.addData(AssetPaths.texture(Util.id("font/" + name)), iconStream);
                ANIMATION_ICONS.put(animation, entry);
            }
        }
        var d = fontAssetBuilder.build().toJson().replace("minecraft:", "");
        resourcePackBuilder.addData("assets/emotive/font/emotes.json", d.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] getIcon(String iconName) {
        try (var is = Emotive.class.getResourceAsStream("/model/emotive/" + iconName + ".png")) {
            if (is != null) {
                try {
                    return is.readAllBytes();
                } catch (IOException e) {
                    return DEFAULT_ICON;
                }
            } else {
                var f = FabricLoader.getInstance().getConfigDir().resolve("emotive/models/" + iconName);
                var file = f.toFile();
                if (file.exists()) {
                    try (var stream = new FileInputStream(file)) {
                        return stream.readAllBytes();
                    } catch (Exception e) {
                        return DEFAULT_ICON;
                    }
                }
            }
        } catch (Exception e) {}

        return DEFAULT_ICON;
    }
}
