package com.cobblemonislands.emotive.configui.impl.selection;

import com.cobblemonislands.emotive.configui.api.ConfiguredGui;
import com.cobblemonislands.emotive.configui.api.GuiElementData;
import com.cobblemonislands.emotive.configui.api.GuiElementType;
import com.cobblemonislands.emotive.config.ConfiguredAnimation;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.world.item.Items;

public class NextPageButton implements GuiElementType<GuiElementData, ConfiguredAnimation> {
    @Override
    public GuiElementBuilder build(ConfiguredGui<GuiElementData, ConfiguredAnimation> g, GuiElementData data) {
        if (g.getCurrentPage("emotes") == g.maxPage("emotes")) {
            return new GuiElementBuilder(Items.AIR.getDefaultInstance());
        }

        return data.decorate(new GuiElementBuilder(data.item().copy())).setCallback(() -> g.nextPage(data.type()));
    }
}
