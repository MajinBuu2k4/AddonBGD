package zgoly.meteorist.modules.FlyGotoMine;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import zgoly.meteorist.Meteorist;
import zgoly.meteorist.modules.movement.LicenseProtectedModule;


import java.util.function.Consumer;

public class FlyGotoMine extends LicenseProtectedModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> targetX = sgGeneral.add(new DoubleSetting.Builder()
            .name("toado-x").description("Toạ độ X cần bay đến").defaultValue(0).build());

    private final Setting<Double> targetY = sgGeneral.add(new DoubleSetting.Builder()
            .name("toado-y").description("Toạ độ Y cần bay đến").defaultValue(120).build());

    private final Setting<Double> targetZ = sgGeneral.add(new DoubleSetting.Builder()
            .name("toado-z").description("Toạ độ Z cần bay đến").defaultValue(0).build());

    private Vec3d target, currentPhaseTarget;
    private int phase = 0;

    private EscapePhase0Handler escape0;
    private EscapePhase1Handler escape1;

    private boolean waitingToFly = true;
    private int flyTicks = 0;
    private Vec3d lastPos = null;

    public FlyGotoMine() {
        super(Meteorist.Custom, "fly-goto-mine", "Tự động bay đến tọa độ, chống kẹt.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null) return;

        ChatUtils.sendPlayerMsg("/fly");
        waitingToFly = true;
        flyTicks = 0;

        target = new Vec3d(targetX.get(), targetY.get(), targetZ.get());
        phase = 0;
        currentPhaseTarget = new Vec3d(mc.player.getX(), target.y, mc.player.getZ());

        escape0 = null;
        escape1 = null;
    }

    @Override
    public void onDeactivate() {
        if (mc.player != null) mc.player.setVelocity(Vec3d.ZERO);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        // ✨ Double-tap logic
        if (waitingToFly) {
            flyTicks++;
            if (flyTicks == 2 || flyTicks == 6) mc.options.jumpKey.setPressed(true);
            else if (flyTicks == 3 || flyTicks == 7) mc.options.jumpKey.setPressed(false);

            if (flyTicks > 20 && !mc.player.getAbilities().flying) {
                flyTicks = 0;
                ChatUtils.sendPlayerMsg("/fly");
                System.out.println("[FlyGotoMine] Gửi lại lệnh /fly để bật bay.");
            }

            if (mc.player.getAbilities().flying) {
                waitingToFly = false;
                System.out.println("[FlyGotoMine] ✅ Đã vào chế độ bay.");
            }
            return;
        }

        // 🛠 Escape handlers
        if (escape0 != null) {
            escape0.tick();
            return;
        }

        if (escape1 != null) {
            escape1.tick();
            return;
        }

        // 📌 Kẹt detection
        Vec3d now = mc.player.getPos();
        boolean stuck = lastPos != null && now.squaredDistanceTo(lastPos) < 0.01;
        lastPos = now;

        if (phase == 0 && (isBlockedAbove() || stuck)) {
            System.out.println("[FlyGotoMine] Kẹt trên cao, gọi EscapePhase0.");
            escape0 = new EscapePhase0Handler(0.6, () -> {
                escape0 = null;
                currentPhaseTarget = new Vec3d(mc.player.getX(), target.y, mc.player.getZ());
                System.out.println("[FlyGotoMine] ✅ EscapePhase0 xong.");
            });
            return;
        }

        if (phase == 1 && (isBlockedFront() || isBlockedLeft() || isBlockedRight())) {
            System.out.println("[FlyGotoMine] Kẹt ngang, gọi EscapePhase1.");
            escape1 = new EscapePhase1Handler(
                    0.6, target,
                    (newPhase) -> {
                        escape1 = null;
                        phase = newPhase;
                        currentPhaseTarget = new Vec3d(mc.player.getX(), target.y, mc.player.getZ());
                        System.out.println("[FlyGotoMine] ✅ EscapePhase1 xong. Quay về phase " + newPhase);
                    }
            );
            return;
        }

        // 🔄 PHASE CHUYỂN ĐỔI
        boolean reachedTarget = Math.abs(now.x - currentPhaseTarget.x) < 0.5 &&
                Math.abs(now.y - currentPhaseTarget.y) < 0.5 &&
                Math.abs(now.z - currentPhaseTarget.z) < 0.5;

        if (reachedTarget) {
            switch (phase) {
                case 0 -> {
                    phase = 1;
                    currentPhaseTarget = new Vec3d(target.x, mc.player.getY(), target.z);
                }
                case 1 -> {
                    phase = 2;
                    currentPhaseTarget = target;
                }
                case 2 -> {
                    System.out.println("[FlyGotoMine] ✅ Đã đến đích — tắt module.");
                    toggle();
                    return;
                }
            }
        }

        Vec3d dir = currentPhaseTarget.subtract(now).normalize();
        mc.player.setVelocity(dir.x * 0.6, dir.y * 0.6, dir.z * 0.6);
    }

    // 🔍 Check kẹt các hướng
    private boolean isBlockedAbove() {
        return !mc.world.getBlockState(mc.player.getBlockPos().up()).isAir();
    }

    private boolean isBlockedFront() {
        Vec3d look = mc.player.getRotationVec(1.0F);
        BlockPos p = mc.player.getBlockPos().add(
                (int) Math.round(look.x), (int) Math.round(look.y), (int) Math.round(look.z));
        return !mc.world.getBlockState(p).isAir();
    }

    private boolean isBlockedLeft() {
        Vec3d left = mc.player.getRotationVec(1.0F).crossProduct(new Vec3d(0, 1, 0));
        BlockPos p = mc.player.getBlockPos().add(
                (int) Math.round(left.x), 0, (int) Math.round(left.z));
        return !mc.world.getBlockState(p).isAir();
    }

    private boolean isBlockedRight() {
        Vec3d right = mc.player.getRotationVec(1.0F).crossProduct(new Vec3d(0, -1, 0));
        BlockPos p = mc.player.getBlockPos().add(
                (int) Math.round(right.x), 0, (int) Math.round(right.z));
        return !mc.world.getBlockState(p).isAir();
    }
}
