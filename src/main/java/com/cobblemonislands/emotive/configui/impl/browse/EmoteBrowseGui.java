package com.cobblemonislands.emotive.configui.impl.browse;

import com.cobblemonislands.emotive.configui.api.ConfiguredGui;
import com.cobblemonislands.emotive.configui.api.GuiElementData;
import com.cobblemonislands.emotive.configui.api.GuiTypeRegistry;
import com.cobblemonislands.emotive.configui.impl.confirm.CancelButton;
import com.cobblemonislands.emotive.configui.impl.selection.EmoteSelectionGui;
import com.cobblemonislands.emotive.configui.impl.selection.EmptyGuiType;
import com.cobblemonislands.emotive.configui.impl.selection.NextPageButton;
import com.cobblemonislands.emotive.configui.impl.selection.PrevPageButton;
import com.cobblemonislands.emotive.config.Animations;
import com.cobblemonislands.emotive.config.ConfiguredAnimation;
import com.cobblemonislands.emotive.config.ModConfig;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class EmoteBrowseGui extends ConfiguredGui<GuiElementData, ConfiguredAnimation> {
    public EmoteBrowseGui(ServerPlayer player) {
        super("emote_browse", ModConfig.getInstance().browseGui, player, false);
    }

    @Override
    protected void build() {
        GuiTypeRegistry.register(this.getGuiId(), "empty", new EmptyGuiType());
        GuiTypeRegistry.register(this.getGuiId(), "emotes", new EmoteListType());
        GuiTypeRegistry.register(this.getGuiId(), "prev_page", new PrevPageButton());
        GuiTypeRegistry.register(this.getGuiId(), "next_page", new NextPageButton());
        GuiTypeRegistry.register(this.getGuiId(), "back", new CancelButton());

        super.build();
    }

    @Override
    protected List<ConfiguredAnimation> getElementsForType(String typeName, ServerPlayer player) {
        if (typeName.equals("emotes")) {
            return new ArrayList<>(Animations.all().values());
        } else if (typeName.equals("favourites")) {
            return new ArrayList<>();
        }

        return super.getElementsForType(typeName, player);
    }

    @Override
    public void back() {
        final var gui = new EmoteSelectionGui(player);
        gui.open();
    }
}
