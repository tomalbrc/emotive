package com.cobblemonislands.emotive.config;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemonislands.emotive.Emotive;
import com.cobblemonislands.emotive.impl.TextUtil;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Objects;

public record ConfiguredAnimation(String title, String animationName, int duration, ResourceLocation item, boolean glint) {
    public ItemStack itemStack() {
        ItemStack itemStack;

        if (item == null)
            itemStack = Items.ROTTEN_FLESH.getDefaultInstance();
        else
            itemStack = BuiltInRegistries.ITEM.get(item).getDefaultInstance();

        itemStack.set(DataComponents.ITEM_NAME, TextUtil.parse(title));

        return itemStack;
    }

    public GuiElementBuilder guiElementBuilder() {
        var style = Style.EMPTY.withItalic(false).withColor(ChatFormatting.WHITE);
        var builder = GuiElementBuilder.from(itemStack());
        builder.addLoreLine(Component.empty());
        builder.addLoreLine(Component.empty().append(Component.literal("⌚").withStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GOLD)).append(Component.literal(" Duration: ").append(duration(duration())).withStyle(style))));
        builder.addLoreLine(Component.empty());
        builder.addLoreLine(Component.empty().append(Component.literal("▶").withStyle(Style.EMPTY.withItalic(false).withColor(ChatFormatting.GREEN)).append(Component.literal(" Press ").append(Component.keybind("key.pickItem")).append(" to play").withStyle(style))));
        return builder;
    }

    private String duration(int duration) {
        String formatted;
        if (duration == -1) {
            formatted = "Until stopped";
        } else {
            formatted = duration + "s";
        }

        return formatted;
    }

    public String permission(ResourceLocation id) {
        return Emotive.MODID + "." + id.toLanguageKey();
    }
}
