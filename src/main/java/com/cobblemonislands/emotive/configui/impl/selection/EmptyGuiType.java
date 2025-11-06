package com.cobblemonislands.emotive.configui.impl.selection;

import com.cobblemonislands.emotive.configui.api.ConfiguredGui;
import com.cobblemonislands.emotive.configui.api.GuiElementData;
import com.cobblemonislands.emotive.configui.api.GuiElementType;
import com.cobblemonislands.emotive.config.ConfiguredAnimation;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.world.item.Items;

public class EmptyGuiType implements GuiElementType<GuiElementData, ConfiguredAnimation> {
    @Override
    public GuiElementBuilder build(ConfiguredGui<GuiElementData, ConfiguredAnimation> player, GuiElementData data) {
        return data.decorate(new GuiElementBuilder(Items.AIR.getDefaultInstance()));
    }
}
