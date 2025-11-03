package com.cobblemonislands.emotive.impl;

import com.cobblemon.mod.common.entity.npc.NPCEntity;
import com.mojang.math.Axis;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Consumer;

public class GestureCameraHolder extends ElementHolder {
    private final BlockDisplayElement cameraElement = new BlockDisplayElement() {
        @Override
        public void notifyMove(Vec3 oldPos, Vec3 newPos, Vec3 delta) {
            if (this.getHolder() != null) {
                var packet = VirtualEntityUtils.createMovePacket(this.getEntityId(), Vec3.ZERO, getCurrentPos(), true, this.getYaw(), this.getPitch());
                this.getHolder().sendPacket(packet);
            }
        }
    };

    private final ServerPlayer player;
    private final Vec3 origin;
    private final NPCEntity playerModel;

    private float pitch;
    private float yaw;
    private boolean dirtyRot = true;

    public GestureCameraHolder(ServerPlayer player, NPCEntity playerModel) {
        super();

        this.playerModel = playerModel;
        this.origin = player.position();
        this.player = player;
        this.updatePos();
        this.cameraElement.setTeleportDuration(2);
        this.cameraElement.ignorePositionUpdates();
        this.addElement(cameraElement);
    }

    public void setPitch(float pitch) {
        if (pitch != this.pitch) {
            this.pitch = pitch;
            this.dirtyRot = true;
        }
    }

    public void setYaw(float yaw) {
        if (yaw != this.yaw) {
            this.yaw = yaw;
            this.dirtyRot = true;
        }
    }

    @Override
    public boolean startWatching(ServerGamePacketListenerImpl player) {
        player.send(new ClientboundUpdateMobEffectPacket(this.player.getId(), new MobEffectInstance(MobEffects.INVISIBILITY, 72000, 0, true, false), false));
        return super.startWatching(player);
    }

    @Override
    public boolean stopWatching(ServerGamePacketListenerImpl player) {
        MobEffectInstance effect = this.player.getEffect(MobEffects.INVISIBILITY);
        if (effect == null)
            player.send(new ClientboundRemoveMobEffectPacket(this.player.getId(), MobEffects.INVISIBILITY));
        else
            player.send(new ClientboundUpdateMobEffectPacket(this.player.getId(), effect, false));

        return super.stopWatching(player);
    }

    @Override
    protected void startWatchingExtraPackets(ServerGamePacketListenerImpl player, Consumer<Packet<ClientGamePacketListener>> packetConsumer) {
        super.startWatchingExtraPackets(player, packetConsumer);
        if (this.player.connection == player) {
            packetConsumer.accept(VirtualEntityUtils.createRidePacket(cameraElement.getEntityId(), IntList.of(player.player.getId())));
            packetConsumer.accept(VirtualEntityUtils.createSetCameraEntityPacket(cameraElement.getEntityId()));
            packetConsumer.accept(new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, GameType.SPECTATOR.getId()));
        }
    }

    @Override
    public void onTick() {
        super.onTick();

        if (this.player == null || this.player.isRemoved() || !GestureController.GESTURE_CAMS.containsKey(player.getUUID())) {
            this.destroy();
        } else {
            this.updatePos();
        }
    }

    @Override
    public void destroy() {
        super.destroy();

        this.playerModel.discard();
    }

    private void updatePos() {
        Vector3f rotatedPoint = currentPoint(-4);

        if (this.getAttachment() != null && dirtyRot) {
            var level = this.getAttachment().getWorld();

            var eyePos = this.origin.add(0, this.player.getEyeHeight()/2.f, 0);
            var blockHitResult = level.clip(new ClipContext(eyePos, new Vec3(rotatedPoint), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
            var adjustedPos = currentPoint(-(Math.max(Mth.abs((float) blockHitResult.getLocation().distanceTo(eyePos)) - 0.5f, 0.1f)));

            var packet = VirtualEntityUtils.createMovePacket(this.cameraElement.getEntityId(), Vec3.ZERO, new Vec3(adjustedPos), true, this.yaw-180, this.pitch);
            this.player.connection.send(packet);

            this.dirtyRot = false;
        }
    }

    private Vector3f currentPoint(float dist) {
        Quaternionf xr = Axis.XP.rotationDegrees(this.pitch).normalize();
        Quaternionf yr = Axis.YN.rotationDegrees(this.yaw + 180).normalize();

        Vector3f off = this.origin.toVector3f().add(0, this.player.getEyeHeight()/2.f, 0);
        return new Vector3f(0, 0, dist).rotate(yr.mul(xr)).add(off);
    }

    public NPCEntity getPlayerModel() {
        return playerModel;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public Vec3 getOrigin() {
        return origin;
    }

    public int getCameraId() {
        return this.cameraElement.getEntityId();
    }
}
