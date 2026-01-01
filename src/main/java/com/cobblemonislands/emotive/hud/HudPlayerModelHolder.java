package com.cobblemonislands.emotive.hud;

import com.cobblemon.mod.fabric.CobblemonFabric;
import de.tomalbrc.bil.core.holder.positioned.PositionedHolder;
import de.tomalbrc.bil.core.holder.wrapper.Bone;
import de.tomalbrc.bil.core.holder.wrapper.DisplayWrapper;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.core.model.Pose;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Brightness;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Objects;

class HudPlayerModelHolder extends PositionedHolder {
    private final ServerPlayer player;
    private int age = 0;

    public Matrix4fc mat = new Matrix4f();

    public boolean active = false;

    public HudPlayerModelHolder(ServerLevel level, ServerPlayer player, Model model) {
        super(level, player.getEyePosition(), model);
        this.player = player;
    }

    @Override
    protected void onAsyncTick() {
        super.onAsyncTick();
        this.age++;
    }

    @Override
    public boolean startWatching(ServerGamePacketListenerImpl p) {
        return p.player == player && super.startWatching(p);
    }

    @Override
    protected void applyPose(Pose pose, DisplayWrapper<?> display) {
        display.element().setBillboardMode(Display.BillboardConstraints.CENTER);
        display.element().setBrightness(Brightness.FULL_BRIGHT);
        display.element().setGlowing(true);

        var mmm = new Matrix4f();
        mmm.translate(mat.getTranslation(new Vector3f()));
        mmm.rotateLocal(mat.getNormalizedRotation(new Quaternionf()));

        var ma = mmm.get(new Matrix4f()).translate(0, 0, 0).translateLocal(0, 0.05f, 0).mul(poseToMatrix(pose).scaleLocal(scale, new Matrix4f()).rotateLocalY(Mth.DEG_TO_RAD * ((age * 2) % 360)));
        display.element().setTransformation(ma.rotateY(Mth.PI));
        display.element().startInterpolationIfDirty();
    }

    public static Matrix4f poseToMatrix(Pose pose) {
        Matrix4f matrix = new Matrix4f().identity();
        matrix.translate(pose.translation());
        matrix.rotate(pose.leftRotation());
        matrix.rotate(pose.rightRotation());
        matrix.scale(pose.scale());
        return matrix;
    }

    public void playAnimationLoop(String name) {
        this.getAnimator().playAnimation(name, (p) -> {
            if (active) Objects.requireNonNull(CobblemonFabric.INSTANCE.server()).execute(() -> {
                playAnimationLoop(name);
            });
        });
    }

    public void setActive(boolean b) {
        this.active = b;

        if (active) {
            asyncTick();
            tick();
            for (Bone bone : this.getBones()) {
                bone.element().setInterpolationDuration(2);
                bone.element().setTeleportDuration(2);
                bone.setInvisible(false);
            }
        } else {
            for (Bone bone : this.getBones()) {
                bone.element().setInterpolationDuration(0);
                bone.element().setTeleportDuration(0);
                bone.setInvisible(true);
            }
            asyncTick();
            tick();
        }
    }
}
