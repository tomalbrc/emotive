package com.cobblemonislands.emotive.configui.impl.selection;

import com.cobblemonislands.emotive.configui.api.ConfiguredGui;
import com.cobblemonislands.emotive.configui.api.GuiElementData;
import com.cobblemonislands.emotive.configui.api.ListGuiElementType;
import com.cobblemonislands.emotive.configui.impl.confirm.ConfirmationGui;
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
        return null;
    }

    @Override
    public GuiElementBuilder buildEntry(ConfiguredGui<GuiElementData, ConfiguredAnimation> gui, GuiElementData data, ConfiguredAnimation element) {
        ItemStack stack = element.itemStack().copy();
        return data.decorate(new GuiElementBuilder(stack), element.placeholder()).setCallback((slot, click, action) -> {
            if (click == ClickType.MOUSE_LEFT) {
                GestureController.onStart(gui.getPlayer(), element);
                gui.close();
            } else if (click == ClickType.MOUSE_LEFT_SHIFT) {
                if (ModConfig.getInstance().getStorage().addFav(gui.getPlayer(), findAnimationId(element)))
                    gui.setPage("favourites", 0);
            } else {
                var gui2 = new ConfirmationGui(gui.getPlayer(), findAnimationId(element));
                gui2.open();
            }
        });
    }

    public static ResourceLocation findAnimationId(ConfiguredAnimation anim) {
        for (Map.Entry<ResourceLocation, ConfiguredAnimation> e : Animations.all().entrySet()) {
            if (e.getValue() == anim) return e.getKey();
        }
        return null;
    }
}
