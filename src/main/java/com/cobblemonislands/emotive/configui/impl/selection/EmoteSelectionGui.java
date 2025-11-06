package com.cobblemonislands.emotive.configui.impl.selection;

import com.cobblemonislands.emotive.config.Animations;
import com.cobblemonislands.emotive.config.ConfiguredAnimation;
import com.cobblemonislands.emotive.config.ModConfig;
import com.cobblemonislands.emotive.configui.api.ConfiguredGui;
import com.cobblemonislands.emotive.configui.api.GuiElementData;
import com.cobblemonislands.emotive.configui.api.GuiTypeRegistry;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EmoteSelectionGui extends ConfiguredGui<GuiElementData, ConfiguredAnimation> {
    public EmoteSelectionGui(ServerPlayer player) {
        super("emote_selection", ModConfig.getInstance().selectionGui, player, false);
    }

    @Override
    protected void build() {
        GuiTypeRegistry.register(this.getGuiId(), "empty", new EmptyGuiType());
        GuiTypeRegistry.register(this.getGuiId(), "emotes", new EmoteListType());
        GuiTypeRegistry.register(this.getGuiId(), "browse", new BrowseButton());
        GuiTypeRegistry.register(this.getGuiId(), "favourites", new FavouriteListType());
        GuiTypeRegistry.register(this.getGuiId(), "prev_page", new PrevPageButton());
        GuiTypeRegistry.register(this.getGuiId(), "next_page", new NextPageButton());

        super.build();
    }

    @Override
    protected List<ConfiguredAnimation> getElementsForType(String typeName, ServerPlayer player) {
        if (typeName.equals("emotes")) {
            return Animations.all().entrySet().stream()
                    .filter(e -> ModConfig.getInstance().getStorage().owns(player, e.getKey()))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
        } else if (typeName.equals("favourites")) {
            var favs = ModConfig.getInstance().getStorage().listFavs(player);
            return Animations.all().entrySet().stream().filter(xx -> favs.contains(xx.getKey().toLanguageKey())).map(Map.Entry::getValue).toList();
        }

        return super.getElementsForType(typeName, player);
    }
}
