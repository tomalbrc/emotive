package com.cobblemonislands.emotive.component;

import com.cobblemonislands.emotive.config.Animations;
import com.cobblemonislands.emotive.config.ModConfig;
import com.cobblemonislands.emotive.util.TextUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public record EmotiveToken(
        @NotNull ResourceLocation id,
        @Nullable String permission,
        @Nullable Integer permissionLevel
) implements TooltipProvider {
    public static final Codec<EmotiveToken> CODEC = RecordCodecBuilder.create(instance -> instance.group(ResourceLocation.CODEC.fieldOf("id").forGetter(EmotiveToken::id), Codec.STRING.optionalFieldOf("permission").forGetter(token -> java.util.Optional.ofNullable(token.permission())), Codec.INT.optionalFieldOf("permission_level").forGetter(token -> java.util.Optional.ofNullable(token.permissionLevel()))).apply(instance, (id, permissionOpt, levelOpt) -> new EmotiveToken(id, permissionOpt.orElse(null), levelOpt.orElse(null))));

    public boolean canUse(ServerPlayer player) {
        if (permission == null) return permissionLevel == null || player.hasPermissions(permissionLevel);
        return Permissions.check(player, permission, permissionLevel == null ? 0 : permissionLevel);
    }

    @Override
    public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
        consumer.accept(TextUtil.parse(String.format(ModConfig.getInstance().messages.componentTooltip, Animations.all().get(this.id).title())));
    }
}
