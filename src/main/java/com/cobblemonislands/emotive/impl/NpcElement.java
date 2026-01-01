package com.cobblemonislands.emotive.impl;

import com.cobblemon.mod.common.CobblemonEntities;
import com.cobblemon.mod.common.entity.npc.NPCPlayerModelType;
import com.cobblemon.mod.common.entity.npc.NPCPlayerTexture;
import com.cobblemon.mod.common.net.messages.client.animation.PlayPosableAnimationPacket;
import com.cobblemon.mod.common.net.messages.client.effect.RunPosableMoLangPacket;
import com.cobblemon.mod.common.net.messages.client.spawn.SpawnNPCPacket;
import com.cobblemon.mod.fabric.CobblemonFabric;
import com.cobblemon.mod.fabric.net.CobblemonFabricNetworkManager;
import com.cobblemonislands.emotive.config.ConfiguredAnimation;
import eu.pb4.placeholders.api.TextParserUtils;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.api.elements.GenericEntityElement;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class NpcElement extends GenericEntityElement {
    int npc = VirtualEntityUtils.requestEntityId();
    UUID npcUUID = UUID.randomUUID();
    ConfiguredAnimation config;
    ModelData modelData;

    public NpcElement(ConfiguredAnimation conf, ModelData modelData, float visualRotationYInDegrees) {
        super();
        this.modelData = modelData;
        this.config = conf;
        this.setYaw(visualRotationYInDegrees);
    }

    @Override
    protected EntityType<? extends Entity> getEntityType() {
        return EntityType.BLOCK_DISPLAY;
    }

    @Override
    public void startWatching(ServerPlayer player, Consumer<Packet<ClientGamePacketListener>> packetConsumer) {
        super.startWatching(player, packetConsumer);

        Set<String> aspects = new HashSet<>(config.aspects());
        aspects.add(modelData.modelAspect());

        var vanillaSpawnPacket = new ClientboundAddEntityPacket(npc, npcUUID, this.getHolder().getPos().x, this.getHolder().getPos().y, this.getHolder().getPos().z, 0, getYaw(), CobblemonEntities.NPC, 0, Vec3.ZERO, getYaw());
        var packet = new SpawnNPCPacket(
                config.npcClass(), // npcClass
                config.resourceIdentifier(), // resourceIdentifier
                aspects, // aspects
                config.level(), // level
                Set.of(),
                TextParserUtils.formatText(config.npcName()), // name
                config.poseType(),
                new NPCPlayerTexture(modelData.texture().getTexture(), NPCPlayerModelType.DEFAULT),
                config.hideNametag(), // hide nametag
                config.width(), // hitbox w
                config.height(), // hitbox h
                config.eyeHeight(), // hitbox eyeHeight
                config.hitboxScale(), // hitbox scale
                config.renderScale(), // render scale
                vanillaSpawnPacket
        );

        CobblemonFabricNetworkManager.INSTANCE.sendPacketToPlayer(player, packet);

        var p2 = new RunPosableMoLangPacket(npc, Set.of("player_texture_username=" + player.getScoreboardName()));
        CobblemonFabricNetworkManager.INSTANCE.sendPacketToPlayer(player, p2);

        CompletableFuture.runAsync(() -> {
            if (!player.hasDisconnected() && config.animationName() != null) {
                var animPacket = new PlayPosableAnimationPacket(npc, Set.of(config.animationName()), config.expressions());
                CobblemonFabricNetworkManager.INSTANCE.sendPacketToPlayer(player, animPacket);
            }
        }, CompletableFuture.delayedExecutor(50, TimeUnit.MILLISECONDS, CobblemonFabric.INSTANCE.server()));
    }

    @Override
    public void stopWatching(ServerPlayer player, Consumer<Packet<ClientGamePacketListener>> packetConsumer) {
        packetConsumer.accept(new ClientboundRemoveEntitiesPacket(IntList.of(npc)));
        super.stopWatching(player, packetConsumer);
    }

    @Override
    public void setYaw(float yaw) {
        if (this.getHolder() != null) {
            var packet = VirtualEntityUtils.createMovePacket(this.npc, Objects.requireNonNullElse(this.lastSyncedPos, Vec3.ZERO), this.getCurrentPos().subtract(0, 0.5, 0), true, yaw, 0);
            if (this.getHolder() != null)
                this.getHolder().sendPacket(packet);
        }
        super.setYaw(yaw);
    }
}
