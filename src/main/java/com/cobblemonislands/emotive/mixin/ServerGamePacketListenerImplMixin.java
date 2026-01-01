package com.cobblemonislands.emotive.mixin;

import com.cobblemonislands.emotive.Emotive;
import com.cobblemonislands.emotive.component.ModComponents;
import com.cobblemonislands.emotive.config.Animations;
import com.cobblemonislands.emotive.config.ModConfig;
import com.cobblemonislands.emotive.impl.GestureController;
import com.cobblemonislands.emotive.util.TextUtil;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;

    @Inject(method = "handlePlayerInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setPlayerInput(FFZZ)V"), cancellable = true)
    private void emotive$handleInput(ServerboundPlayerInputPacket serverboundPlayerInputPacket, CallbackInfo ci) {
        var camera = GestureController.GESTURE_CAMS.get(player.getUUID());
        if (camera != null) {
            if (serverboundPlayerInputPacket.isShiftKeyDown()) {
                GestureController.onStop(camera);
            }

            ci.cancel();
        }
    }

    @Inject(method = "handlePlayerAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;resetLastActionTime()V"), cancellable = true)
    private void emotive$handleAction(ServerboundPlayerActionPacket serverboundPlayerActionPacket, CallbackInfo ci) {
        if (serverboundPlayerActionPacket.getAction() == ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND && player.isShiftKeyDown()) {
            Emotive.openHud(player);
            ci.cancel();
        }
    }

    @Inject(method = "handleMovePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;containsInvalidValues(DDDFF)Z"), cancellable = true)
    private void emotive$handleMove(ServerboundMovePlayerPacket serverboundMovePlayerPacket, CallbackInfo ci) {
        var camera = GestureController.GESTURE_CAMS.get(player.getUUID());
        if (camera != null) {
            if (serverboundMovePlayerPacket instanceof ServerboundMovePlayerPacket.Rot rot) {
                camera.setYaw(rot.getYRot(this.player.getYRot()));
                camera.setPitch(rot.getXRot(this.player.getXRot()));
            }
            ci.cancel();
        }
    }

    @Inject(method = "handleInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;serverLevel()Lnet/minecraft/server/level/ServerLevel;", ordinal = 0), cancellable = true)
    private void emotive$preventInteract(ServerboundInteractPacket serverboundInteractPacket, CallbackInfo ci) {
        if (GestureController.GESTURE_CAMS.containsKey(player.getUUID())) {
            ci.cancel();
        }
    }

    @Inject(method = "teleport(DDDFFLjava/util/Set;)V", at = @At("HEAD"))
    private void emotive$preventTeleport(double d, double e, double f, float g, float h, Set<RelativeMovement> set, CallbackInfo ci) {
        if (GestureController.GESTURE_CAMS.containsKey(player.getUUID())) {
            GestureController.onStop(player);
        }
    }

    @Inject(method = "handleUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;useItem(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"), cancellable = true)
    private void emotive$backpackInteraction(ServerboundUseItemPacket serverboundUseItemPacket, CallbackInfo ci, @Local ItemStack itemStack) {
        if (itemStack.has(ModComponents.EMOTIVE_TOKEN)) {
            var tokenId = itemStack.get(ModComponents.EMOTIVE_TOKEN);
            if (tokenId != null && tokenId.canUse(player) && ModConfig.getInstance().getStorage().add(player, tokenId.id())) {
                player.sendSystemMessage(TextUtil.parse(String.format(ModConfig.getInstance().messages.added, Animations.all().get(tokenId.id()).title())));
                itemStack.consume(1, player);
            }
            ci.cancel();
        }
    }

    @Inject(method = "handleUseItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;useItemOn(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"), cancellable = true)
    private void emotive$backpackInteraction(ServerboundUseItemOnPacket serverboundUseItemOnPacket, CallbackInfo ci, @Local ItemStack itemStack) {
        if (itemStack.has(ModComponents.EMOTIVE_TOKEN)) {
            var tokenId = itemStack.get(ModComponents.EMOTIVE_TOKEN);
            if (tokenId != null && tokenId.canUse(player) && ModConfig.getInstance().getStorage().add(player, tokenId.id())) {
                player.sendSystemMessage(TextUtil.parse(String.format(ModConfig.getInstance().messages.added, Animations.all().get(tokenId.id()).title())));
                itemStack.consume(1, player);
            }
            ci.cancel();
        }
    }
}