package zgoly.meteorist.modules.WaypointFly;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;
import zgoly.meteorist.Meteorist;

import java.io.*;
import java.lang.reflect.Type;
import java.util.List;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class WaypointFly extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<String>> waypoints = sgGeneral.add(new StringListSetting.Builder()
            .name("waypoints")
            .description("Danh sách các waypoint và delay.")
            .defaultValue(new ArrayList<>())
            .build()
    );

    public final Setting<Double> reachRange = sgGeneral.add(new DoubleSetting.Builder()
            .name("waypoint-reach-range")
            .description("Khoảng cách tối đa để tính là đã đến waypoint.")
            .defaultValue(3)
            .min(0.1)
            .sliderMax(10)
            .build()
    );

    private int currentIndex = 0;
    private int delayTicks = 0;
    private boolean waitingToFly = false;
    private int flyTicks = 0;
    private boolean reachedPreviousWaypoint = true;

    private static final File SAVE_FILE = new File("config/meteorist/waypoints/WaypointFly.json");
    private static final Gson GSON = new Gson();

    public WaypointFly() {
        super(Meteorist.Custom, "waypoint-fly", "Tự động bay qua các waypoint.");
        File folder = SAVE_FILE.getParentFile();
        if (!folder.exists()) folder.mkdirs();
    }

    @Override
    public void onActivate() {
        if (mc.player == null) return;

        ChatUtils.sendPlayerMsg("/fly");
        waitingToFly = true;
        flyTicks = 0;
        currentIndex = 0;
        delayTicks = 0;
        reachedPreviousWaypoint = true;
    }

    @Override
    public void onDeactivate() {
        if (mc.player != null) mc.player.setVelocity(Vec3d.ZERO);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        List<String> wp = waypoints.get();
        if (wp == null || wp.isEmpty()) return;

        // Đợi chế độ bay bật
        if (waitingToFly) {
            flyTicks++;
            if (flyTicks == 2 || flyTicks == 6) mc.options.jumpKey.setPressed(true);
            else if (flyTicks == 3 || flyTicks == 7) mc.options.jumpKey.setPressed(false);

            if (flyTicks > 20 && !mc.player.getAbilities().flying) {
                flyTicks = 0;
                ChatUtils.sendPlayerMsg("/fly");
                info("🔁 Gửi lại lệnh /fly.");
            }

            if (mc.player.getAbilities().flying) {
                waitingToFly = false;
                info("🛫 Đã vào chế độ bay.");
            }
            return;
        }

        if (currentIndex >= wp.size()) {
            info("✅ Đã hoàn thành tất cả waypoint.");
            toggle(); // Tự tắt module
            return;
        }

        String line = wp.get(currentIndex).trim();

        // Xử lý delay
        if (line.toLowerCase().startsWith("delay")) {
            if (!reachedPreviousWaypoint) return;

            if (delayTicks == 0) {
                String[] parts = line.split(" ");
                int seconds = parts.length > 1 ? parseIntOrDefault(parts[1], 3) : 3;
                delayTicks = seconds * 20;
                info("⏱️ Delay " + seconds + " giây.");
            }

            delayTicks--;
            if (delayTicks <= 0) {
                currentIndex++;
                delayTicks = 0;
                reachedPreviousWaypoint = true;
            }
            return;
        }

        // Xử lý waypoint toạ độ
        String[] coords = line.split(" ");
        if (coords.length != 3) {
            warning("⚠️ Waypoint không hợp lệ: " + line);
            currentIndex++;
            return;
        }

        try {
            double x = Double.parseDouble(coords[0]);
            double y = Double.parseDouble(coords[1]);
            double z = Double.parseDouble(coords[2]);

            Vec3d target = new Vec3d(x, y, z);
            Vec3d playerPos = mc.player.getPos();
            double dist = playerPos.distanceTo(target);

            if (dist < reachRange.get()) {
                currentIndex++;
                reachedPreviousWaypoint = true;
            } else {
                reachedPreviousWaypoint = false;
                Vec3d motion = target.subtract(playerPos).normalize().multiply(0.6);
                mc.player.setVelocity(motion);
            }
        } catch (Exception e) {
            warning("⚠️ Lỗi tọa độ: " + line);
            currentIndex++;
        }
    }

    public void saveWaypoints() {
        try (FileWriter writer = new FileWriter(SAVE_FILE)) {
            GSON.toJson(waypoints.get(), writer);
            info("💾 Đã lưu waypoint vào: " + SAVE_FILE.getAbsolutePath());
        } catch (IOException e) {
            error("❌ Không thể lưu file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadWaypoints() {
        if (!SAVE_FILE.exists()) {
            warning("⚠️ File không tồn tại: " + SAVE_FILE.getAbsolutePath());
            return;
        }

        try (FileReader reader = new FileReader(SAVE_FILE)) {
            Type listType = new TypeToken<List<String>>() {}.getType();
            List<String> loaded = GSON.fromJson(reader, listType);
            if (loaded == null) loaded = new ArrayList<>();
            waypoints.set(loaded);
            info("📂 Đã tải " + loaded.size() + " waypoint từ JSON.");
        } catch (Exception e) {
            error("❌ Lỗi khi đọc file JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addWaypoint(String waypoint) {
        List<String> list = new ArrayList<>(waypoints.get() != null ? waypoints.get() : new ArrayList<>());
        list.add(waypoint);
        waypoints.set(list);
        info("📌 Đã thêm waypoint: " + waypoint);
    }

    public void clearWaypoints() {
        waypoints.set(new ArrayList<>());
        info("🧹 Đã xóa tất cả waypoint.");
    }

    public List<String> getWaypoints() {
        return waypoints.get();
    }

    private int parseIntOrDefault(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
