package com.cobblemonislands.emotive.impl;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.entity.npc.NPCPlayerModelType;
import com.cobblemon.mod.common.entity.npc.NPCPlayerTexture;
import com.cobblemonislands.emotive.api.EmoteEvents;
import com.cobblemonislands.emotive.config.ConfiguredAnimation;
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
                            VirtualEntityUtils.createRidePacket(camera.getCameraId(), IntList.of()),
                            new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, player.gameMode.getGameModeForPlayer().getId()),
                            packet,
                            new ClientboundSetCameraPacket(player)
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

        EmoteEvents.START_EMOTE.invoker().onStartEmote(player);

        GestureCameraHolder gestureCameraHolder = new GestureCameraHolder(player);
        gestureCameraHolder.addElement(new NpcElement(animation, data, player.getYRot()));

        GestureController.GESTURE_CAMS.put(player.getUUID(), gestureCameraHolder);
        ChunkAttachment.ofTicking(gestureCameraHolder, player.serverLevel(), player.position());

        CompletableFuture.runAsync(() -> {
            if (GestureController.GESTURE_CAMS.containsValue(gestureCameraHolder))
                onStop(player);
        }, CompletableFuture.delayedExecutor(animation.duration(), TimeUnit.SECONDS, player.server));
    }
}
