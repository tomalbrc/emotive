package com.cobblemonislands.emotive.configui.impl.confirm;

import com.cobblemonislands.emotive.component.EmotiveToken;
import com.cobblemonislands.emotive.component.ModComponents;
import com.cobblemonislands.emotive.config.Animations;
import com.cobblemonislands.emotive.config.ConfiguredAnimation;
import com.cobblemonislands.emotive.config.ModConfig;
import com.cobblemonislands.emotive.configui.api.ConfiguredGui;
import com.cobblemonislands.emotive.configui.api.GuiElementData;
import com.cobblemonislands.emotive.configui.api.GuiTypeRegistry;
import com.cobblemonislands.emotive.configui.impl.selection.EmoteSelectionGui;
import com.cobblemonislands.emotive.configui.impl.selection.EmptyGuiType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ConfirmationGui extends ConfiguredGui<GuiElementData, ConfiguredAnimation> {
    final ResourceLocation emote;

    public ConfirmationGui(ServerPlayer player, ResourceLocation emote) {
        super("emote_confirmation", ModConfig.getInstance().confirmationGui, player, false);
        this.emote = emote;
    }

    @Override
    protected void build() {
        GuiTypeRegistry.register(this.getGuiId(), "empty", new EmptyGuiType());
        GuiTypeRegistry.register(this.getGuiId(), "cancel", new CancelButton());
        GuiTypeRegistry.register(this.getGuiId(), "confirm", new ConfirmButton());

        super.build();
    }

    @Override
    protected List<ConfiguredAnimation> getElementsForType(String typeName, ServerPlayer player) {
        return super.getElementsForType(typeName, player);
    }

    public void confirm() {
        if (ModConfig.getInstance().getStorage().remove(player, emote)) {
            var anim = Animations.all().get(emote);
            if (anim == null) {
                close();
                return;
            }

            ItemStack item = anim.itemStack();
            item.set(ModComponents.EMOTIVE_TOKEN, new EmotiveToken(emote, anim.permission(), anim.permissionLevel()));

            player.addItem(item);

            if (!item.isEmpty() && item.getCount() > 0) {
                player.spawnAtLocation(item);
            }
        }

        back();
    }

    @Override
    public void back() {
        final var gui = new EmoteSelectionGui(player);
        gui.open();
    }
}
