package zgoly.meteorist.modules.FlyGotoMine;

import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.GoalXZ;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;


import java.util.function.Consumer;

public class EscapePhase1Handler {
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final double speed;
    private final Vec3d target;
    private final Consumer<Integer> onSuccess;

    private int stage = 0;
    private int flyTapTicks = 0;
    private boolean baritoneStarted = false;
    private int reachedTargetTicks = 0;

    private Vec3d adjustedXZ;

    public EscapePhase1Handler(double speed, Vec3d target, Consumer<Integer> onSuccess) {
        this.speed = speed;
        this.target = target;
        this.onSuccess = onSuccess;

        Vec3d from = mc.player.getPos();
        Vec3d dir = target.subtract(from).normalize().multiply(-10);
        adjustedXZ = new Vec3d(target.x + dir.x, 0, target.z + dir.z);

        ChatUtils.sendPlayerMsg("/fly");
        System.out.println("[EscapePhase1] 🚫 Đã gửi lệnh /fly để rơi xuống.");
    }

    public void tick() {
        if (mc.player == null || mc.world == null) return;

        switch (stage) {
            case 0 -> {
                if (mc.player.isOnGround()) {
                    System.out.println("[EscapePhase1] ✅ Đã chạm đất.");
                    stage = 1;
                }
            }

            case 1 -> {
                if (!baritoneStarted) {
                    BlockPos goalXZ = new BlockPos((int) adjustedXZ.x, mc.player.getBlockY(), (int) adjustedXZ.z);
                    BaritoneAPI.getProvider().getPrimaryBaritone().getCustomGoalProcess()
                            .setGoalAndPath(new GoalXZ(goalXZ.getX(), goalXZ.getZ()));
                    baritoneStarted = true;
                    System.out.println("[EscapePhase1] 🧭 Baritone đi đến gần target (cách 10 block): " + goalXZ.toShortString());
                }

                double dx = Math.abs(mc.player.getX() - adjustedXZ.x);
                double dz = Math.abs(mc.player.getZ() - adjustedXZ.z);
                if (dx < 2.5 && dz < 2.5) {
                    reachedTargetTicks++;
                    if (reachedTargetTicks >= 10) {
                        BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("stop");
                        System.out.println("[EscapePhase1] ⏸ Baritone đã dừng. Chuẩn bị bay.");
                        ChatUtils.sendPlayerMsg("/fly");
                        flyTapTicks = 0;
                        stage = 2;
                    }
                } else {
                    reachedTargetTicks = 0;
                }
            }

            case 2 -> {
                flyTapTicks++;

                if (flyTapTicks == 2 || flyTapTicks == 6) mc.options.jumpKey.setPressed(true);
                else if (flyTapTicks == 3 || flyTapTicks == 7) mc.options.jumpKey.setPressed(false);

                if (mc.player.getAbilities().flying) {
                    System.out.println("[EscapePhase1] ✅ Bay lại thành công. Trả quyền điều khiển cho FlyGotoMine (phase 0).");
                    stage = 3;
                    onSuccess.accept(0); // Gửi phase 0 về FlyGotoMine
                }

                if (flyTapTicks > 20 && !mc.player.getAbilities().flying) {
                    flyTapTicks = 0;
                    ChatUtils.sendPlayerMsg("/fly");
                    System.out.println("[EscapePhase1] 🔁 Gửi lại /fly vì chưa bay được.");
                }
            }
        }
    }

    public boolean isDone() {
        return stage == 3;
    }
}
