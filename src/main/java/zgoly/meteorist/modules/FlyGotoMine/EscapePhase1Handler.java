package zgoly.meteorist.modules.FlyGotoMine;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class EscapePhase1Handler {
    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final double speed;
    private boolean done = false;
    private boolean climbing = false;
    private int stuckTicks = 0;
    private EscapeState state = EscapeState.TRY_LEFT;

    private enum EscapeState {
        TRY_LEFT,
        CLIMB,
        DONE
    }

    public EscapePhase1Handler(double speed) {
        this.speed = speed;
    }

    public void tick() {
        if (mc.player == null || mc.world == null) return;

        switch (state) {
            case TRY_LEFT -> {
                if (!isBlockedFront()) {
                    done = true;
                    return;
                }

                if (isBlockedLeft()) {
                    state = EscapeState.CLIMB;
                    return;
                }

                moveLeftWithSlightY();

                stuckTicks++;
                if (stuckTicks > 20) {  // Đứng im > 20 tick thì thử leo lên
                    state = EscapeState.CLIMB;
                    stuckTicks = 0;
                }
            }

            case CLIMB -> {
                if (!isBlockedFront()) {
                    done = true;
                    return;
                }

                moveUpWithSlightRandom();

                stuckTicks++;
                if (stuckTicks > 40) {  // Nếu leo lâu quá mà vẫn kẹt, coi như xong
                    done = true;
                }
            }

            case DONE -> {
                done = true;
            }
        }
    }

    private void moveLeftWithSlightY() {
        Vec3d left = mc.player.getRotationVec(1.0F).crossProduct(new Vec3d(0, 1, 0))
                .normalize().multiply(speed);
        mc.player.setVelocity(left.x, 0.05, left.z);
    }

    private void moveUpWithSlightRandom() {
        Vec3d up = new Vec3d(
                0.1 * (Math.random() - 0.5),
                0.5,
                0.1 * (Math.random() - 0.5)
        ).normalize().multiply(speed);
        mc.player.setVelocity(up);
    }

    private boolean isBlockedFront() {
        Vec3d look = mc.player.getRotationVec(1.0F);
        BlockPos front = mc.player.getBlockPos().add(
                (int) Math.floor(look.x * 1.2),
                (int) Math.floor(look.y * 1.2),
                (int) Math.floor(look.z * 1.2)
        );
        BlockState block = mc.world.getBlockState(front);
        return !block.isAir();
    }

    private boolean isBlockedLeft() {
        Vec3d left = mc.player.getRotationVec(1.0F).crossProduct(new Vec3d(0, 1, 0));
        BlockPos pos = mc.player.getBlockPos().add(
                (int) Math.floor(left.x * 1.2),
                0,
                (int) Math.floor(left.z * 1.2)
        );
        return !mc.world.getBlockState(pos).isAir();
    }

    public boolean isDone() {
        return done;
    }
}
