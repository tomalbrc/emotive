package com.cobblemonislands.emotive.impl;

import com.bedrockk.molang.runtime.value.StringValue;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonEntities;
import com.cobblemon.mod.common.api.npc.NPCClasses;
import com.cobblemon.mod.common.entity.npc.NPCEntity;
import com.cobblemon.mod.common.entity.npc.NPCPlayerModelType;
import com.cobblemon.mod.common.entity.npc.NPCPlayerTexture;
import com.cobblemonislands.emotive.mixin.EntityAccessor;
import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class GestureController {
    public static final Reference2ObjectOpenHashMap<UUID, GestureCameraHolder> GESTURE_CAMS = new Reference2ObjectOpenHashMap<>();

    public static final Map<UUID, ModelData> TEXTURE_CACHE = new Object2ObjectOpenHashMap<>();

    public static void onConnect(ServerPlayer serverPlayer) {
        // to have a cached model/texture of players
        MinecraftServer server = Cobblemon.implementation.server();
        if (server == null) return;

        String username = serverPlayer.getScoreboardName();
        server.getProfileRepository().findProfilesByNames(new String[]{username}, new ProfileLookupCallback() {
            @Override
            public void onProfileLookupSucceeded(GameProfile profile) {
                GameProfile deepProfile = server.getSessionService().fetchProfile(profile.getId(), false) != null
                        ? Objects.requireNonNull(server.getSessionService().fetchProfile(profile.getId(), false)).profile()
                        : null;

                if (deepProfile == null) {
                    Cobblemon.LOGGER.error("Failed to fetch profile for game profile name: {}", username);
                    return;
                }

                MinecraftProfileTextures textures = server.getSessionService().getTextures(deepProfile);
                MinecraftProfileTexture skin = textures.skin();
                if (skin == null) return;

                String url = skin.getUrl();
                String modelMeta = skin.getMetadata("model") != null ? skin.getMetadata("model") : "default";
                NPCPlayerModelType model = NPCPlayerModelType.valueOf(Objects.requireNonNull(modelMeta).toUpperCase());

                try {
                    var tex = new NPCPlayerTexture(new URI(url).toURL().openStream().readAllBytes(), model);
                    var aspect = "model-" + model.name().toLowerCase();
                    TEXTURE_CACHE.put(serverPlayer.getUUID(), new ModelData(tex, aspect));
                } catch (IOException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onProfileLookupFailed(String profileName, Exception exception) {
                Cobblemon.LOGGER.error("Unable to load texture for game profile name: {}", username, exception);
            }
        });
    }

    public static void onDisconnect(ServerPlayer serverPlayer) {
        var cam = GESTURE_CAMS.get(serverPlayer.getUUID());
        if (cam != null) {
            onStop(cam);
        }
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

        camera.destroy();
        GestureController.GESTURE_CAMS.remove(player.getUUID());
    }

    public static void onStart(ServerPlayer player, String animationName) {
        // destroy any previous gestures
        GestureCameraHolder camera = GestureController.GESTURE_CAMS.get(player.getUUID());
        if (camera != null) {
            camera.destroy();
        }

        NPCEntity playerModel = CobblemonEntities.NPC.create(player.level());
        assert playerModel != null;
        ((EntityAccessor)(Entity)playerModel).invokeUnsetRemoved();
        playerModel.noPhysics = true;
        playerModel.setNoGravity(true);
        playerModel.setInvulnerable(true);
        playerModel.setNpc(Objects.requireNonNull(NPCClasses.INSTANCE.getByName("standard")));
        playerModel.moveTo(player.position(), player.yHeadRot, player.getXRot());

        playerModel.setYHeadRot(player.yHeadRot);
        playerModel.setYBodyRot(player.yHeadRot);

        var data = TEXTURE_CACHE.get(player.getUUID());
        if (data != null) {
            playerModel.getEntityData().set(NPCEntity.Companion.getNPC_PLAYER_TEXTURE(), data.texture());
            playerModel.getData().setDirectly("player_texture_username", new StringValue(player.getScoreboardName()));
            playerModel.getAppliedAspects().add(data.modelAspect());
            playerModel.updateAspects();
        }

        GestureCameraHolder gestureCameraHolder = new GestureCameraHolder(player, playerModel);
        GestureController.GESTURE_CAMS.put(player.getUUID(), gestureCameraHolder);
        ChunkAttachment.ofTicking(gestureCameraHolder, player.serverLevel(), player.position());

        player.serverLevel().addFreshEntity(playerModel);

        CompletableFuture.runAsync(() -> playerModel.playAnimation(animationName, List.of()), CompletableFuture.delayedExecutor(50, TimeUnit.MILLISECONDS, player.server));

        CompletableFuture.runAsync(() -> {
            if (GestureController.GESTURE_CAMS.containsValue(gestureCameraHolder))
                onStop(player);
        }, CompletableFuture.delayedExecutor(6, TimeUnit.SECONDS, player.server));
    }
}
