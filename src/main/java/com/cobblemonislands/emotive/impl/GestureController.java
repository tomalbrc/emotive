package com.cobblemonislands.emotive.impl;

import com.cobblemon.mod.common.CobblemonEntities;
import com.cobblemon.mod.common.api.npc.NPCClasses;
import com.cobblemon.mod.common.entity.npc.NPCEntity;
import com.google.common.collect.ImmutableList;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class GestureController {
    public static final Reference2ObjectOpenHashMap<UUID, GestureCameraHolder> GESTURE_CAMS = new Reference2ObjectOpenHashMap<>();

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
        playerModel.setNpc(NPCClasses.INSTANCE.getByName("standard"));
        playerModel.moveTo(player.position(), player.getYRot(), player.getXRot());

        GestureCameraHolder gestureCameraHolder = new GestureCameraHolder(player, playerModel);
        GestureController.GESTURE_CAMS.put(player.getUUID(), gestureCameraHolder);

        ChunkAttachment.ofTicking(gestureCameraHolder, player.serverLevel(), player.position());

        CompletableFuture.runAsync(() -> {
            playerModel.loadTextureFromGameProfileName(player.getScoreboardName());
        });

        CompletableFuture.runAsync(() -> {
            playerModel.playAnimation(animationName, List.of());
        }, CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS, player.server));

        CompletableFuture.runAsync(() -> {
            onStop(player);
        }, CompletableFuture.delayedExecutor(10, TimeUnit.SECONDS, player.server));

        player.serverLevel().addFreshEntity(playerModel);
    }
}
