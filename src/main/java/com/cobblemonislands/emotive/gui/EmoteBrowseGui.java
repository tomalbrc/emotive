package com.cobblemonislands.emotive.gui;

import com.cobblemonislands.emotive.config.*;
import com.cobblemonislands.emotive.util.TextUtil;
import com.cobblemonislands.emotive.util.Util;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.layered.Layer;
import eu.pb4.sgui.api.gui.layered.LayeredGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EmoteBrowseGui extends LayeredGui {
    protected final boolean addBackButton;
    protected final ConfiguredCategory category;
    private final Layer contentLayer;
    private int page = 0;

    public EmoteBrowseGui(ServerPlayer player, boolean addBackButton, @Nullable ConfiguredCategory category) {
        super(Util.menuTypeForHeight(ModConfig.getInstance().gui.browseMenuHeight), player, false);
        this.addBackButton = addBackButton;
        this.category = category;

        this.contentLayer = new Layer(this.getHeight() - 2, this.getWidth() - 2);
        addLayer(this.contentLayer, 1, 1);

        this.setTitle(TextUtil.parse(ModConfig.getInstance().gui.browseMenuTitle));
        setupStaticButtons();
        renderPage();
    }

    private void setupStaticButtons() {
        if (this.addBackButton && ModConfig.getInstance().gui.addBackButton) {
            var loc = ModConfig.getInstance().gui.backButtonLocation;
            int idx = GuiHelpers.posToIndex(loc.x - 1, loc.y - 1, getHeight(), getWidth());
            this.setSlot(idx, GuiElementBuilder.from(ModConfig.getInstance().gui.backItem())
                    .setName(TextUtil.parse(ModConfig.getInstance().messages.back))
                    .setCallback(() -> {
                        Util.clickSound(getPlayer());
                        if (category == null) new EmoteSelectionGui(getPlayer(), false).open();
                        else new EmoteBrowseGui(getPlayer(), true, null).open();
                    }));
        }
    }

    private List<GuiElementBuilder> buildElementBuilders() {
        List<GuiElementBuilder> builders = new ArrayList<>();

        if (this.category != null) {
            var available = this.category.animations().entrySet();
            for (var entry : available) {
                var animation = entry.getValue();
                GuiElementBuilder b = animation.guiElementBuilder(false);
                if (ModConfig.getInstance().getStorage().owns(getPlayer(), entry.getKey())) {
                    b.addLoreLine(Component.empty());
                    b.addLoreLine(Component.empty().append(Component.empty().withStyle(ConfiguredAnimation.EMPTY).append(TextUtil.parse(ModConfig.getInstance().messages.alreadyOwning))));
                }
                builders.add(b);
            }
        } else {
            for (Map.Entry<String, ConfiguredCategory> entry : Categories.CATEGORIES.entrySet()) {
                builders.add(entry.getValue().guiElementBuilder().setCallback(() -> new EmoteBrowseGui(getPlayer(), true, entry.getValue()).open()));
            }

            var available = Animations.UNGROUPED;
            for (var entry : available.entrySet()) {
                ConfiguredAnimation animation = entry.getValue();
                GuiElementBuilder b = animation.guiElementBuilder(false);
                if (ModConfig.getInstance().getStorage().owns(getPlayer(), entry.getKey())) {
                    b.addLoreLine(Component.empty());
                    b.addLoreLine(Component.empty().append(Component.empty().withStyle(ConfiguredAnimation.EMPTY).append(TextUtil.parse(ModConfig.getInstance().messages.alreadyOwning))));
                }
                builders.add(b);
            }
        }

        return builders;
    }

    private void renderPage() {
        List<GuiElementBuilder> builders = buildElementBuilders();

        final int interiorWidth = this.getWidth() - 2;
        final int interiorHeight = this.getHeight() - 2;
        final int layerCapacity = interiorWidth * interiorHeight;

        int totalPages = Math.max(1, (int) Math.ceil((double) builders.size() / layerCapacity));
        if (this.page < 0) this.page = 0;
        if (this.page >= totalPages) this.page = totalPages - 1;

        this.contentLayer.clearSlots();

        int start = this.page * layerCapacity;
        int end = Math.min(start + layerCapacity, builders.size());

        for (int i = start; i < end; i++) {
            GuiElementBuilder builder = builders.get(i);
            this.contentLayer.addSlot(builder.build());
        }

        var prevLoc = ModConfig.getInstance().gui.prevButtonLocation;
        int prevIdx = GuiHelpers.posToIndex(prevLoc.x - 1, prevLoc.y - 1, getHeight(), getWidth());

        var nextLoc = ModConfig.getInstance().gui.nextButtonLocation;
        int nextIdx = GuiHelpers.posToIndex(nextLoc.x - 1, nextLoc.y - 1, getHeight(), getWidth());

        if (totalPages > 1) {
            if (this.page > 0) {
                this.setSlot(prevIdx, GuiElementBuilder.from(ModConfig.getInstance().gui.prevItem()).setCallback(() -> {
                    Util.clickSound(getPlayer());
                    this.page = Math.max(0, this.page - 1);
                    renderPage();
                }));
            } else {
                this.setSlot(prevIdx, ItemStack.EMPTY);
            }

            if (this.page < totalPages - 1) {
                this.setSlot(nextIdx, GuiElementBuilder.from(ModConfig.getInstance().gui.nextItem()).setCallback(() -> {
                    Util.clickSound(getPlayer());
                    this.page = Math.min(totalPages - 1, this.page + 1);
                    renderPage();
                }));
            } else {
                this.setSlot(nextIdx, ItemStack.EMPTY);
            }

        } else {
            this.setSlot(prevIdx, ItemStack.EMPTY);
            this.setSlot(nextIdx, ItemStack.EMPTY);
        }
    }
}
