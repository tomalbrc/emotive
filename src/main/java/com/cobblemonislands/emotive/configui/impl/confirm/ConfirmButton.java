package com.cobblemonislands.emotive.configui.impl.confirm;

import com.cobblemonislands.emotive.configui.api.ConfiguredGui;
import com.cobblemonislands.emotive.configui.api.GuiElementData;
import com.cobblemonislands.emotive.configui.api.GuiElementType;
import com.cobblemonislands.emotive.config.ConfiguredAnimation;
import eu.pb4.sgui.api.elements.GuiElementBuilder;

public class ConfirmButton implements GuiElementType<GuiElementData, ConfiguredAnimation> {
    @Override
    public GuiElementBuilder build(ConfiguredGui<GuiElementData, ConfiguredAnimation> g, GuiElementData data) {
        return data.decorate(new GuiElementBuilder(data.item().copy())).setCallback(() -> {
            if (g instanceof ConfirmationGui confirmationGui)
                confirmationGui.confirm();
        });
    }
}
