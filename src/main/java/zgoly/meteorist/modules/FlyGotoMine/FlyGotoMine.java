package zgoly.meteorist.modules.FlyGotoMine;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import zgoly.meteorist.Meteorist;
import zgoly.meteorist.modules.movement.LicenseProtectedModule;

public class FlyGotoMine extends LicenseProtectedModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> targetX = sgGeneral.add(new DoubleSetting.Builder()
            .name("toado-x")
            .description("Toa do X can bay den")
            .defaultValue(0)
            .build()
    );

    private final Setting<Double> targetY = sgGeneral.add(new DoubleSetting.Builder()
            .name("toado-y")
            .description("Toa do Y can bay den")
            .defaultValue(120)
            .build()
    );

    private final Setting<Double> targetZ = sgGeneral.add(new DoubleSetting.Builder()
            .name("toado-z")
            .description("Toa do Z can bay den")
            .defaultValue(0)
            .build()
    );

    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
            .name("toc-do-bay")
            .description("Toc do di chuyen")
            .defaultValue(0.7)
            .min(0.1)
            .sliderMax(2.0)
            .build()
    );

    private Vec3d target;
    private Vec3d currentPhaseTarget;
    private int phase = 0; // 0: lên, 1: ngang, 2: xuống
    private EscapePhase0Handler escape0;
    private EscapePhase1Handler escape1;

    private int stuckTicks = 0;
    private Vec3d lastPos = null;

    public FlyGotoMine() {
        super(Meteorist.Custom, "fly-goto-mine", "Tu dong bay den toa do, tu chong ket.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null) return;
        target = new Vec3d(targetX.get(), targetY.get(), targetZ.get());
        phase = 0;
        currentPhaseTarget = new Vec3d(mc.player.getX(), target.y, mc.player.getZ());
        escape0 = null;
        escape1 = null;
        stuckTicks = 0;
        lastPos = null;
    }

    @Override
    public void onDeactivate() {
        mc.player.setVelocity(Vec3d.ZERO);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        // Escape Phase 0 Handler
        if (escape0 != null) {
            escape0.tick();
            if (escape0.isDone()) {
                escape0 = null;
                stuckTicks = 0;
                currentPhaseTarget = new Vec3d(mc.player.getX(), target.y, mc.player.getZ());
            }
            return;
        }

        // Escape Phase 1 Handler
        if (escape1 != null) {
            escape1.tick();
            if (escape1.isDone()) {
                escape1 = null;
                currentPhaseTarget = new Vec3d(target.x, mc.player.getY(), target.z);
            }
            return;
        }

        // Tính khoảng cách di chuyển để phát hiện kẹt
        Vec3d now = mc.player.getPos();
        if (lastPos != null && now.squaredDistanceTo(lastPos) < 0.01) {
            stuckTicks++;
        } else {
            stuckTicks = 0;
        }
        lastPos = now;

        // Nếu phase 0 và bị chặn trên đầu hoặc đứng im quá 20 tick => gọi EscapePhase0
        if (phase == 0 && (isBlockedAbove() || stuckTicks >= 20)) {
            escape0 = new EscapePhase0Handler(speed.get());
            return;
        }

        // Nếu phase 1 và bị chặn trước mặt hoặc bên trái/phải => gọi EscapePhase1
        if (phase == 1 && (isBlockedFront() || isBlockedLeft() || isBlockedRight())) {
            escape1 = new EscapePhase1Handler(speed.get());
            return;
        }

        // Di chuyển theo phase hiện tại
        Vec3d pos = mc.player.getPos();
        double distance = pos.distanceTo(currentPhaseTarget);

        if (distance < 1.0) {
            if (phase == 0) {
                phase = 1;
                currentPhaseTarget = new Vec3d(target.x, mc.player.getY(), target.z);
            } else if (phase == 1) {
                phase = 2;
                currentPhaseTarget = target;
            } else {
                toggle(); // Kết thúc
                return;
            }
        }

        Vec3d dir = currentPhaseTarget.subtract(pos).normalize().multiply(speed.get());
        mc.player.setVelocity(dir);
    }

    private boolean isBlockedAbove() {
        BlockPos head = mc.player.getBlockPos().up();
        return !mc.world.getBlockState(head).isAir();
    }

    private boolean isBlockedFront() {
        Vec3d look = mc.player.getRotationVec(1.0F);
        BlockPos front = mc.player.getBlockPos().add(
                (int) Math.round(look.x),
                (int) Math.round(look.y),
                (int) Math.round(look.z)
        );
        return !mc.world.getBlockState(front).isAir();
    }

    private boolean isBlockedLeft() {
        Vec3d left = mc.player.getRotationVec(1.0F).crossProduct(new Vec3d(0,1,0));
        BlockPos pos = mc.player.getBlockPos().add(
                (int) Math.round(left.x),
                0,
                (int) Math.round(left.z)
        );
        return !mc.world.getBlockState(pos).isAir();
    }

    private boolean isBlockedRight() {
        Vec3d right = mc.player.getRotationVec(1.0F).crossProduct(new Vec3d(0,-1,0));
        BlockPos pos = mc.player.getBlockPos().add(
                (int) Math.round(right.x),
                0,
                (int) Math.round(right.z)
        );
        return !mc.world.getBlockState(pos).isAir();
    }
}
