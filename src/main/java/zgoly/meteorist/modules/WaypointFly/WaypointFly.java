package zgoly.meteorist.modules.WaypointFly;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class WaypointFly extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
            .name("speed")
            .description("Tốc độ bay.")
            .defaultValue(0.6)
            .min(0.1)
            .max(5)
            .sliderMax(2)
            .build()
    );

    private final Setting<Double> reachDistance = sgGeneral.add(new DoubleSetting.Builder()
            .name("reach-distance")
            .description("Khoảng cách tối thiểu để tính là đã đến waypoint.")
            .defaultValue(1)
            .min(0.1)
            .max(5)
            .sliderMax(2)
            .build()
    );

    private List<Vec3d> waypoints;
    private int currentIndex = 0;

    // Fly toggle logic
    private boolean waitingToFly = true;
    private int flyTicks = 0;

    public WaypointFly() {
        super(zgoly.meteorist.Meteorist.Custom, "waypoint-fly", "Bay theo danh sách waypoint đã lưu bằng lệnh .wpf.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null) {
            error("❌ Không tìm thấy player.");
            toggle();
            return;
        }

        waypoints = WaypointManager.getWaypoints();
        if (waypoints.isEmpty()) {
            error("📭 Chưa có waypoint nào. Dùng lệnh .wpf add trước.");
            toggle();
            return;
        }

        // Gửi lệnh /fly
        ChatUtils.sendPlayerMsg("/fly");
        waitingToFly = true;
        flyTicks = 0;

        currentIndex = 0;
        ChatUtils.info("✈️ Bắt đầu bay đến " + waypoints.size() + " waypoint.");
    }

    @Override
    public void onDeactivate() {
        if (mc.player != null) {
            mc.player.setVelocity(Vec3d.ZERO);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        // 🚀 Auto /fly handler
        if (waitingToFly) {
            flyTicks++;
            if (flyTicks == 2 || flyTicks == 6) mc.options.jumpKey.setPressed(true);
            else if (flyTicks == 3 || flyTicks == 7) mc.options.jumpKey.setPressed(false);

            if (flyTicks > 20 && !mc.player.getAbilities().flying) {
                flyTicks = 0;
                ChatUtils.sendPlayerMsg("/fly");
                System.out.println("[WaypointFly] Gửi lại lệnh /fly để bật bay.");
            }

            if (mc.player.getAbilities().flying) {
                waitingToFly = false;
                System.out.println("[WaypointFly] ✅ Đã vào chế độ bay.");
            }
            return;
        }

        // 🧭 Fly logic
        if (currentIndex >= waypoints.size()) {
            ChatUtils.info("✅ Đã hoàn thành đường bay!");
            toggle();
            return;
        }

        Vec3d playerPos = mc.player.getPos();
        Vec3d target = waypoints.get(currentIndex);

        double distance = playerPos.distanceTo(target);
        if (distance < reachDistance.get()) {
            currentIndex++;
            if (currentIndex < waypoints.size()) {
                ChatUtils.info("📍 Đến waypoint [" + currentIndex + "/" + waypoints.size() + "]");
            }
            return;
        }

        Vec3d dir = target.subtract(playerPos).normalize();
        mc.player.setVelocity(dir.multiply(speed.get()));
    }
}
