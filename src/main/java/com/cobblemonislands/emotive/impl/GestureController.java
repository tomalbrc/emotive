package com.cobblemonislands.emotive.impl;

import com.bedrockk.molang.runtime.value.StringValue;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonEntities;
import com.cobblemon.mod.common.api.npc.NPCClasses;
import com.cobblemon.mod.common.entity.npc.NPCEntity;
import com.cobblemon.mod.common.entity.npc.NPCPlayerModelType;
import com.cobblemon.mod.common.entity.npc.NPCPlayerTexture;
import com.cobblemonislands.emotive.api.EmoteEvents;
import com.cobblemonislands.emotive.config.ConfiguredAnimation;
import com.cobblemonislands.emotive.config.ModConfig;
import com.cobblemonislands.emotive.mixin.EntityAccessor;
import com.cobblemonislands.emotive.util.Util;
import com.google.common.collect.ImmutableList;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class GestureController {
    public static final Map<UUID, GestureCameraHolder> GESTURE_CAMS = new ConcurrentHashMap<>();
    public static final Map<UUID, ModelData> TEXTURE_CACHE = new ConcurrentHashMap<>();

    public static CompletableFuture<ModelData> getOrCreateModelData(ServerPlayer player) {

        var cached = TEXTURE_CACHE.getOrDefault(player.getUUID(), null);
        
        if(cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        MinecraftServer server = player.server;

        return CompletableFuture.supplyAsync(() -> {

            var gameProfile = player.getGameProfile();

            MinecraftProfileTextures textures = server.getSessionService().getTextures(gameProfile);
            MinecraftProfileTexture skin = textures.skin();
            if (skin == null) return null;

            String url = skin.getUrl();
            String modelMeta = skin.getMetadata("model") != null ? skin.getMetadata("model") : "default";
            NPCPlayerModelType model = NPCPlayerModelType.valueOf(Objects.requireNonNull(modelMeta).toUpperCase());

            try {
                var tex = new NPCPlayerTexture(new URI(url).toURL().openStream().readAllBytes(), model);
                var aspect = "model-" + model.name().toLowerCase();
                var modelData = new ModelData(tex, aspect);
                TEXTURE_CACHE.put(player.getUUID(), new ModelData(tex, aspect));
                return modelData;
            } catch (IOException | URISyntaxException e) {
                Cobblemon.LOGGER.error("Could not load texture for {}", player.getName(), e);
                return null;
            }

        });

    }

    public static void onDisconnect(ServerPlayer serverPlayer) {
        var cam = GESTURE_CAMS.get(serverPlayer.getUUID());
        if (cam != null) {
            onStop(cam);
        }
        TEXTURE_CACHE.remove(serverPlayer.getUUID());
    }

    public static void onStop(ServerPlayer player) {
        var cam = GestureController.GESTURE_CAMS.get(player.getUUID());
        if (cam != null) {
            GestureController.onStop(cam);
        }
    }

    public static void onStop(GestureCameraHolder camera) {
        var player = camera.getPlayer();
        if (!player.hasDisconnected()) {
            PolymerUtils.reloadInventory(player);
            for (ServerGamePacketListenerImpl watchingPlayer : camera.getWatchingPlayers()) {
                watchingPlayer.send(new ClientboundSetEquipmentPacket(player.getId(), Util.getEquipment(player, false)));
            }

            List<SynchedEntityData.DataValue<?>> data = new ObjectArrayList<>();
            data.add(SynchedEntityData.DataValue.create(EntityTrackedData.FLAGS, player.getEntityData().get(EntityTrackedData.FLAGS)));
            camera.getWatchingPlayers().forEach(p -> camera.sendPacket(new ClientboundSetEntityDataPacket(player.getId(), data)));

            var packet = new ClientboundPlayerPositionPacket(camera.getOrigin().x, camera.getOrigin().y, camera.getOrigin().z, player.getYRot(), player.getXRot(), Set.of(), player.getId());
            player.connection.send(
                    new ClientboundBundlePacket(ImmutableList.of(
                            new ClientboundSetCameraPacket(player),
                            VirtualEntityUtils.createRidePacket(camera.getCameraId(), IntList.of()),
                            new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, player.gameMode.getGameModeForPlayer().getId()),
                            packet
                    ))
            );
        }

        EmoteEvents.STOP_EMOTE.invoker().onStopEmote(player);

        camera.destroy();
        GestureController.GESTURE_CAMS.remove(player.getUUID());
    }

    public static void onStart(ServerPlayer player, ConfiguredAnimation animation, ModelData data) {
        if (GestureController.GESTURE_CAMS.containsKey(player.getUUID())) // prevent spamming gestures
            return;

        // destroy any previous gestures
        GestureCameraHolder camera = GestureController.GESTURE_CAMS.get(player.getUUID());
        if (camera != null) {
            camera.destroy();
        }

        NPCEntity playerModel = CobblemonEntities.NPC.create(player.level());
        assert playerModel != null;
        ((EntityAccessor) (Entity) playerModel).invokeUnsetRemoved();
        playerModel.noPhysics = true;
        playerModel.setNoGravity(true);
        playerModel.setMovable(false);
        playerModel.setLeashable(false);
        playerModel.setAllowProjectileHits(false);
        playerModel.setInvulnerable(true);
        playerModel.setNpc(Objects.requireNonNull(NPCClasses.INSTANCE.getByIdentifier(animation.npcClass())));
        playerModel.moveTo(player.position(), player.yHeadRot, player.getXRot());
        playerModel.setHideNameTag(!ModConfig.getInstance().showPlayerName);
        playerModel.setCustomName(player.getDisplayName());
        playerModel.setCustomNameVisible(ModConfig.getInstance().showPlayerName);
        //playerModel.getVariationAspects().add("");

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            var item = player.getItemBySlot(slot);
            playerModel.setItemSlot(slot, item);
        }

        playerModel.setItemInHand(InteractionHand.MAIN_HAND, player.getMainHandItem());
        playerModel.setItemInHand(InteractionHand.OFF_HAND, player.getOffhandItem());

        playerModel.setXRot(player.getXRot());
        playerModel.setYHeadRot(player.yHeadRot);
        playerModel.setYBodyRot(player.yHeadRot);

        if (data != null) {
            playerModel.getEntityData().set(NPCEntity.Companion.getNPC_PLAYER_TEXTURE(), data.texture());
            playerModel.getData().setDirectly("player_texture_username", new StringValue(player.getScoreboardName()));
            playerModel.getAppliedAspects().add(data.modelAspect());
            playerModel.updateAspects();
        }

        EmoteEvents.START_EMOTE.invoker().onStartEmote(player);

        GestureCameraHolder gestureCameraHolder = new GestureCameraHolder(player, playerModel);
        GestureController.GESTURE_CAMS.put(player.getUUID(), gestureCameraHolder);
        ChunkAttachment.ofTicking(gestureCameraHolder, player.serverLevel(), player.position());

        player.serverLevel().addFreshEntity(playerModel);

        CompletableFuture.runAsync(() -> playerModel.playAnimation(animation.animationName(), List.of()), CompletableFuture.delayedExecutor(50, TimeUnit.MILLISECONDS, player.server));

        CompletableFuture.runAsync(() -> {
            if (GestureController.GESTURE_CAMS.containsValue(gestureCameraHolder))
                onStop(player);
        }, CompletableFuture.delayedExecutor(animation.duration(), TimeUnit.SECONDS, player.server));
    }
}
