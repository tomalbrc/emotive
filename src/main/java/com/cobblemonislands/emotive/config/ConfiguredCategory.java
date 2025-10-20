package com.cobblemonislands.emotive.config;

import com.cobblemonislands.emotive.util.TextUtil;
import com.google.common.collect.ImmutableMap;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;

import java.util.List;
import java.util.Map;

public record ConfiguredCategory(
        String id,
        String title,
        ResourceLocation item,
        List<String> lore,
        Integer customModelData,
        boolean glint,
        Map<ResourceLocation, ConfiguredAnimation> animations
) {
    public ItemStack itemStack() {
        ItemStack itemStack;

        if (item == null)
            itemStack = Items.ROTTEN_FLESH.getDefaultInstance();
        else
            itemStack = BuiltInRegistries.ITEM.get(item).getDefaultInstance();

        itemStack.set(DataComponents.ITEM_NAME, Component.empty().append(Component.empty().withStyle(ConfiguredAnimation.EMPTY).append(TextUtil.parse(title))));
        if (customModelData != null)
            itemStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(customModelData));

        return itemStack;
    }

    public GuiElementBuilder guiElementBuilder() {
        var builder = GuiElementBuilder.from(itemStack());
        if (lore != null) for (String string : lore) {
            builder.addLoreLine(Component.empty().append(Component.empty().withStyle(ConfiguredAnimation.EMPTY).append(TextUtil.parse(string))));
        }
        builder.glow(glint());
        return builder;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String title;
        private ResourceLocation item;
        private final List<String> lore = new ObjectArrayList<>();
        private Integer customModelData;
        private boolean glint;
        private final Map<ResourceLocation, ConfiguredAnimation> animations = new Object2ObjectOpenHashMap<>();

        private Builder() {}

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setItem(ResourceLocation item) {
            this.item = item;
            return this;
        }

        public Builder setCustomModelData(Integer customModelData) {
            this.customModelData = customModelData;
            return this;
        }

        public Builder setGlint(boolean glint) {
            this.glint = glint;
            return this;
        }

        public Builder addAnimation(ResourceLocation id, ConfiguredAnimation animation) {
            this.animations.put(id, animation);
            return this;
        }

        public Builder addAnimations(Map<ResourceLocation, ConfiguredAnimation> animations) {
            this.animations.putAll(animations);
            return this;
        }

        public Builder addLore(String lore) {
            this.lore.add(lore);
            return this;
        }

        public Builder addLore(List<String> lore) {
            this.lore.addAll(lore);
            return this;
        }

        public ConfiguredCategory build() {
            return new ConfiguredCategory(
                    id,
                    title,
                    item,
                    lore,
                    customModelData,
                    glint,
                    ImmutableMap.copyOf(animations) // immutable copy
            );
        }
    }
}