package zgoly.meteorist.modules.FlyGotoMine;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class EscapePhase0Handler {
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final double speed;
    private boolean done = false;
    private int clearTicks = 0;
    private int moveTicks = 0;
    private Vec3d currentDirection;

    public EscapePhase0Handler(double speed) {
        this.speed = speed;
        this.currentDirection = getRandomHorizontal();
    }

    public void tick() {
        if (mc.player == null || mc.world == null) return;

        BlockPos head = mc.player.getBlockPos().up();
        BlockState block = mc.world.getBlockState(head);

        if (!block.isAir()) {
            clearTicks = 0;
            move();
            return;
        }

        // Nếu đầu không bị chặn nữa
        clearTicks++;
        if (clearTicks >= 10) {
            done = true;
        } else {
            move();
        }
    }

    private void move() {
        // Đổi hướng sau mỗi 10 tick
        if (moveTicks <= 0) {
            currentDirection = getRandomHorizontal();
            moveTicks = 5;
        }

        mc.player.setVelocity(currentDirection);
        moveTicks--;
    }

    private Vec3d getRandomHorizontal() {
        int dir = (int) (Math.random() * 4);
        return switch (dir) {
            case 0 -> new Vec3d(speed, 0, 0);   // phải
            case 1 -> new Vec3d(-speed, 0, 0);  // trái
            case 2 -> new Vec3d(0, 0, speed);   // tiến
            case 3 -> new Vec3d(0, 0, -speed);  // lùi
            default -> new Vec3d(0, 0, 0);
        };
    }

    public boolean isDone() {
        return done;
    }
}
