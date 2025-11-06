package com.cobblemonislands.emotive.configui.impl.selection;

import com.cobblemonislands.emotive.config.Animations;
import com.cobblemonislands.emotive.configui.api.ConfiguredGui;
import com.cobblemonislands.emotive.configui.api.GuiElementData;
import com.cobblemonislands.emotive.configui.api.ListGuiElementType;
import com.cobblemonislands.emotive.config.ConfiguredAnimation;
import com.cobblemonislands.emotive.config.ModConfig;
import com.cobblemonislands.emotive.impl.GestureController;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.world.item.ItemStack;

class FavouriteListType implements ListGuiElementType<GuiElementData, ConfiguredAnimation> {
    @Override
    public GuiElementBuilder build(ConfiguredGui<GuiElementData, ConfiguredAnimation> gui, GuiElementData data) {
        return new GuiElementBuilder(ItemStack.EMPTY);
    }

    @Override
    public GuiElementBuilder buildEntry(ConfiguredGui<GuiElementData, ConfiguredAnimation> gui, GuiElementData data, ConfiguredAnimation element) {
        var id = EmoteListType.findAnimationId(element);
        return data.decorate(new GuiElementBuilder(element.itemStack()), element.placeholder()).setCallback((s, c, a) -> {
            if (c == eu.pb4.sgui.api.ClickType.MOUSE_LEFT_SHIFT) {
                ModConfig.getInstance().getStorage().removeFav(gui.getPlayer(), id);
                gui.setPage(data.type(), 0);
            } else {
                var player = gui.getPlayer();
                GestureController.getOrCreateModelData(player).thenAccept(modelData -> {
                    player.server.execute(() -> GestureController.onStart(player, Animations.all().get(id), modelData));
                    gui.close();
                });
            }
        });
    }
}
