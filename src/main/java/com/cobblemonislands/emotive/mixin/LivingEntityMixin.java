package com.cobblemonislands.emotive.mixin;

import com.cobblemonislands.emotive.impl.GestureController;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "equipmentHasChanged", at = @At("RETURN"), cancellable = true)
    private void emotive$onEquipmentChanged(ItemStack itemStack, ItemStack itemStack2, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ServerPlayer serverPlayer && GestureController.GESTURE_CAMS.containsKey(serverPlayer.getUUID())) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "hurt", at = @At("HEAD"))
    private void emotive$handleDamage(DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ServerPlayer serverPlayer) {
            GestureController.onStop(serverPlayer);
        }
    }
}
