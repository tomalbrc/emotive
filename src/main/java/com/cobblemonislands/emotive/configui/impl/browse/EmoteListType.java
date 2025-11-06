package com.cobblemonislands.emotive.configui.impl.browse;

import com.cobblemonislands.emotive.configui.api.ConfiguredGui;
import com.cobblemonislands.emotive.configui.api.GuiElementData;
import com.cobblemonislands.emotive.configui.api.ListGuiElementType;
import com.cobblemonislands.emotive.config.Animations;
import com.cobblemonislands.emotive.config.ConfiguredAnimation;
import com.cobblemonislands.emotive.config.ModConfig;
import com.cobblemonislands.emotive.impl.GestureController;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

class EmoteListType implements ListGuiElementType<GuiElementData, ConfiguredAnimation> {
    @Override
    public GuiElementBuilder build(ConfiguredGui<GuiElementData, ConfiguredAnimation> player, GuiElementData data) {
        return new GuiElementBuilder();
    }

    @Override
    public GuiElementBuilder buildEntry(ConfiguredGui<GuiElementData, ConfiguredAnimation> gui, GuiElementData data, ConfiguredAnimation element) {
        ItemStack stack = element.itemStack().copy();
        return data.decorate(new GuiElementBuilder(stack).setCallback((slot, click, action) -> {
            if (click == ClickType.MOUSE_LEFT && ModConfig.getInstance().getStorage().owns(gui.getPlayer(), findAnimationId(element))) {
                GestureController.onStart(gui.getPlayer(), element);
                gui.close();
            }
        }), element.placeholder(), ModConfig.getInstance().getStorage().owns(gui.getPlayer(), findAnimationId(element)));
    }

    public static ResourceLocation findAnimationId(ConfiguredAnimation anim) {
        for (Map.Entry<ResourceLocation, ConfiguredAnimation> e : Animations.all().entrySet()) {
            if (e.getValue() == anim) return e.getKey();
        }
        return null;
    }
}
