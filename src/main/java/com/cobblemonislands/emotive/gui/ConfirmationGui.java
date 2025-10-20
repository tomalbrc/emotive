package com.cobblemonislands.emotive.gui;

import com.cobblemonislands.emotive.component.EmotiveToken;
import com.cobblemonislands.emotive.component.ModComponents;
import com.cobblemonislands.emotive.config.Animations;
import com.cobblemonislands.emotive.config.ModConfig;
import com.cobblemonislands.emotive.storage.LPStorage;
import com.cobblemonislands.emotive.util.TextUtil;
import com.cobblemonislands.emotive.util.Util;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ConfirmationGui extends SimpleGui {
    private final ResourceLocation emote;
    private final Runnable onClose;

    public ConfirmationGui(ServerPlayer player, ResourceLocation emote, Runnable onClose) {
        super(Util.menuTypeForHeight(ModConfig.getInstance().gui.confirmationMenuHeight), player, false);

        this.onClose = onClose;
        this.emote = emote;
        this.setTitle(TextUtil.parse(ModConfig.getInstance().gui.confirmationMenuTitle));

        setupButtons();
    }

    private void setupButtons() {
        var gui = ModConfig.getInstance().gui;

        if (gui.addBackButton) {
            var loc = gui.backButtonLocation;
            int idx = GuiHelpers.posToIndex(loc.x - 1, loc.y - 1, getHeight(), getWidth());
            this.setSlot(idx, GuiElementBuilder.from(gui.backItem())
                    .setName(TextUtil.parse(ModConfig.getInstance().messages.cancel))
                    .setCallback(this::cancel));
        }

        var confirmLoc = gui.confirmButtonLocation != null ? gui.confirmButtonLocation : gui.browseButtonLocation; // fallback
        int confirmIdx = GuiHelpers.posToIndex(confirmLoc.x - 1, confirmLoc.y - 1, getHeight(), getWidth());
        this.setSlot(confirmIdx, GuiElementBuilder.from(gui.confirmItem())
                .setName(TextUtil.parse(ModConfig.getInstance().messages.confirm))
                .setCallback(this::confirm));
    }

    private void confirm() {
        Util.clickSound(player);

        if (LPStorage.remove(player, emote)) {
            var anim = Animations.UNGROUPED.get(emote);
            if (anim == null) {
                close();
                return;
            }

            ItemStack item = anim.itemStack();
            item.set(ModComponents.EMOTIVE_TOKEN, new EmotiveToken(emote, anim.permission(), anim.permissionLevel()));

            if (!item.isEmpty() && item.getCount() > 0) {
                player.spawnAtLocation(item);
            }
        }

        this.close();
        if (this.onClose != null) this.onClose.run();
    }

    private void cancel() {
        Util.clickSound(player);
        this.close();
        if (this.onClose != null) this.onClose.run();
    }
}
