package com.cobblemonislands.emotive.config;

import com.cobblemonislands.emotive.util.TextUtil;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;

public record ConfiguredAnimation(String title, ResourceLocation npcClass, String animationName, int duration, ResourceLocation item, Integer customModelData, boolean glint, String permission, Integer permissionLevel) {
    public static Style EMPTY = Style.EMPTY.withColor(ChatFormatting.WHITE).withUnderlined(false).withItalic(false).withObfuscated(false).withStrikethrough(false);

    public ItemStack itemStack() {
        ItemStack itemStack;

        if (item == null)
            itemStack = Items.ROTTEN_FLESH.getDefaultInstance();
        else
            itemStack = BuiltInRegistries.ITEM.get(item).getDefaultInstance();

        itemStack.set(DataComponents.ITEM_NAME, Component.empty().append(Component.empty().withStyle(EMPTY).append(TextUtil.parse(title))));
        if (customModelData != null)
            itemStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(customModelData));

        return itemStack;
    }

    public GuiElementBuilder guiElementBuilder() {
        return guiElementBuilder(true);
    }

    public GuiElementBuilder guiElementBuilder(boolean playable) {
        var builder = GuiElementBuilder.from(itemStack());
        builder.addLoreLine(Component.empty());
        builder.addLoreLine(Component.empty().append(Component.empty().withStyle(EMPTY).append(TextUtil.parse(String.format(ModConfig.getInstance().messages.emoteDurationTooltip, duration(duration))))));
        if (playable) {
            builder.addLoreLine(Component.empty());
            builder.addLoreLine(Component.empty().append(Component.empty().withStyle(EMPTY).append(TextUtil.parse(ModConfig.getInstance().messages.emotePlayTooltip))));

            builder.addLoreLine(Component.empty());
            builder.addLoreLine(Component.empty().append(Component.empty().withStyle(EMPTY).append(TextUtil.parse(ModConfig.getInstance().messages.emoteGetItemTooltip))));
        }
        builder.glow(glint());
        return builder;
    }

    private String duration(int duration) {
        String formatted;
        if (duration == -1) {
            formatted = ModConfig.getInstance().messages.untilStopped;
        } else {
            formatted = duration + "s";
        }

        return formatted;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String title;
        private ResourceLocation npcClass = ResourceLocation.fromNamespaceAndPath("cobblemon", "standard");
        private String animationName;
        private int duration = 6;
        private ResourceLocation item = ResourceLocation.withDefaultNamespace("diamond");
        private Integer customModelData = null;
        private boolean glint;
        private int permissionLevel;
        private String permission;

        private Builder() {}

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder npcClass(ResourceLocation npcClass) {
            this.npcClass = npcClass;
            return this;
        }

        public Builder animationName(String animationName) {
            this.animationName = animationName;
            return this;
        }

        public Builder duration(int duration) {
            this.duration = duration;
            return this;
        }

        public Builder item(ResourceLocation item) {
            this.item = item;
            return this;
        }

        public Builder glint(boolean glint) {
            this.glint = glint;
            return this;
        }

        public Builder permission(String permission) {
            this.permission = permission;
            return this;
        }

        public Builder permissionLevel(int level) {
            this.permissionLevel = level;
            return this;
        }

        public Builder customModelData(int customModelData) {
            this.customModelData = customModelData;
            return this;
        }

        public ConfiguredAnimation build() {
            return new ConfiguredAnimation(title, npcClass, animationName, duration, item, customModelData, glint, permission, permissionLevel);
        }
    }
}
