package com.cobblemonislands.emotive.gui;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemonislands.emotive.command.EmotiveCommand;
import com.cobblemonislands.emotive.config.ConfiguredAnimation;
import com.cobblemonislands.emotive.config.ModConfig;
import com.cobblemonislands.emotive.impl.GestureController;
import com.cobblemonislands.emotive.impl.TextUtil;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.layered.Layer;
import eu.pb4.sgui.api.gui.layered.LayeredGui;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.List;
import java.util.Map;

public class EmoteSelectionGui extends LayeredGui {
    public EmoteSelectionGui(MenuType<?> type, ServerPlayer player, boolean manipulatePlayerSlots) {
        super(type, player, manipulatePlayerSlots);

        this.setTitle(TextUtil.parse(ModConfig.getInstance().messages.selectionMenuTitle));
        setup();
    }

    private List<ConfiguredAnimation> availableAnimations() {
        List<ConfiguredAnimation> res = new ObjectArrayList<>();
        for (Map.Entry<ResourceLocation, ConfiguredAnimation> entry : ModConfig.getInstance().animations.entrySet()) {
            if (Permissions.check(this.getPlayer(), entry.getValue().permission(entry.getKey()), 2)) {
                res.add(entry.getValue());
            }
        }

        return res;
    }

    void setup() {
        this.addSlot(GuiElementBuilder.from(Items.ARROW.getDefaultInstance()).setName(TextUtil.parse("Back")).setCallback(() -> {
            this.close();
        }));

        var available = availableAnimations();

        Layer layer = new Layer(this.getHeight() - 2, this.getWidth() - 2);
        for (var animation : available) {
            GuiElementBuilder builder = animation.guiElementBuilder();

            builder.setCallback(() -> {
                GestureController.onStart(getPlayer(), animation.animationName());
            });

            if (animation.glint()) {
                builder.enchant(Cobblemon.implementation.server(), Enchantments.VANISHING_CURSE, 1);
            }

            layer.addSlot(builder.build());
        }
        addLayer(layer, 1, 1);
    }
}
