package zgoly.meteorist.modules.WaypointFly;

import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.systems.modules.Modules;
import zgoly.meteorist.Meteorist;


import net.minecraft.util.math.Vec3d;
import zgoly.meteorist.modules.movement.LicenseProtectedModule;

import java.util.ArrayList;
import java.util.List;

public class ReturnToStart extends LicenseProtectedModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<String>> actions = sgGeneral.add(new StringListSetting.Builder()
            .name("actions")
            .description("Danh sach hanh dong thuc hien khi quay ve spawn.")
            .defaultValue(new ArrayList<>())
            .build()
    );

    private final Setting<Double> reachRange = sgGeneral.add(new DoubleSetting.Builder()
            .name("spawn-range")
            .description("Khoang cach toi da tinh la da den diem POS/GOTO.")
            .defaultValue(2.0)
            .min(0.1)
            .sliderMax(10)
            .build()
    );

    private final Setting<Boolean> enableWaypointFly = sgGeneral.add(new BoolSetting.Builder()
            .name("bat-waypointfly")
            .description("Bat WaypointFly sau khi hoan thanh.")
            .defaultValue(false)
            .build()
    );
    private final Setting<Boolean> enableSpawnCheck = sgGeneral.add(new BoolSetting.Builder()
            .name("check-spawn-position")
            .description("Chi chay khi dang o gan toa do spawn/mine.")
            .defaultValue(false)
            .build()
    );

    private final Setting<String> spawnPosition = sgGeneral.add(new StringSetting.Builder()
            .name("spawn-position-check")
            .description("Toa do kiem tra, dinh dang: x y z (VD: 15 0 25)")
            .defaultValue("0 0 0")
            .visible(enableSpawnCheck::get)
            .build()
    );

    private final Setting<Double> spawnRangeCheck = sgGeneral.add(new DoubleSetting.Builder()
            .name("spawn-check-range")
            .description("Khoang cach toi da de bat dau thuc thi lenh.")
            .defaultValue(2.0)
            .min(0.1)
            .sliderMax(20)
            .visible(enableSpawnCheck::get)
            .build()
    );

    private final Setting<Integer> spawnCheckTimeout = sgGeneral.add(new IntSetting.Builder()
            .name("spawn-check-timeout")
            .description("Thoi gian (giay) cho nguoi choi o gan toa do spawn.")
            .defaultValue(5)
            .min(1)
            .sliderMax(30)
            .visible(enableSpawnCheck::get)
            .build()
    );



    private AntiStuckDetector antiStuck;
    private final List<WaypointEntry> entries = new ArrayList<>();
    private int currentIndex = 0;
    private int delayTicks = 0;
    private int gotoTickCounter = 0;
    private boolean reachedPreviousWaypoint = true;
    private int flyTickCounter = 0;
    private boolean flyRequested = false;
    private boolean waitingFlyOffLanding = false;
    private boolean waitingSpawnCheck = false;
    private int spawnCheckTicks = 0;


    public ReturnToStart() {
        super(Meteorist.Custom, "return-to-start", "Di chuyen tu spawn/mine ve waypoint hoac diem dau.");
    }

    @Override
    public void onActivate() {
        super.onActivate(); // ✅ Kiểm tra license
        if (!isActive()) return;
        if (mc.player == null) return;

        if (enableSpawnCheck.get()) {
            Vec3d spawnPos = parsePosition(spawnPosition.get());
            if (spawnPos == null) {
                warning("❌ Toa do spawn khong hop le.");
                toggle();
                return;
            }

            // Bắt đầu quá trình đợi kiểm tra vị trí spawn
            waitingSpawnCheck = true;
            spawnCheckTicks = 0;
            info("⌛ Dang kiem tra vi tri spawn trong 5 giay...");
            return;
        }

        // Nếu không bật check vị trí spawn thì vào thẳng chuỗi hành động
        startSequence();
    }


    @Override
    public void onDeactivate() {
        if (mc.player != null) mc.player.setVelocity(Vec3d.ZERO);
        meteordevelopment.meteorclient.MeteorClient.EVENT_BUS.unsubscribe(this);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        // 1. Chờ kiểm tra vị trí spawn (nếu enableSpawnCheck bật)
        if (waitingSpawnCheck) {
            spawnCheckTicks++;
            Vec3d spawnPos = parsePosition(spawnPosition.get());

            if (spawnPos == null) {
                warning("❌ Toa do spawn khong hop le.");
                toggle();
                return;
            }

            double dist = mc.player.getPos().distanceTo(spawnPos);
            if (dist <= spawnRangeCheck.get()) {
                info(String.format("✅ Dang o gan spawn (%.2f blocks). Bat dau chay.", dist));
                waitingSpawnCheck = false;
                startSequence(); // chạy tiếp
                return;
            }

            if (spawnCheckTicks >= spawnCheckTimeout.get() * 20) {
                warning("🚫 Khong o gan spawn sau 5 giay. Huy module.");
                toggle();
            }

            return; // chưa đủ điều kiện thì chưa xử lý gì tiếp
        }

        // 2. Xử lý FLYOFF hạ cánh
        if (waitingFlyOffLanding) {
            if (mc.player.isOnGround()) {
                waitingFlyOffLanding = false;
                info("✅ Da tiep dat, tiep tuc waypoint.");
            } else {
                return;
            }
        }

        // 3. Đợi bật fly
        if (flyRequested) {
            handleFlyOnTick();
            return;
        }

        // 4. Đã chạy xong toàn bộ chuỗi
        if (currentIndex >= entries.size()) {
            finishSequence();
            return;
        }

        // 5. Thực hiện hành động hiện tại
        WaypointEntry entry = entries.get(currentIndex);

        switch (entry.type) {
            case POS -> handlePos(entry);
            case GOTO -> handleGoto(entry);
            case DELAY -> handleDelay(entry);
            case FLYON -> handleFlyOn();
            case FLYOFF -> handleFlyOff();
            case COMMAND -> {
                ChatUtils.sendPlayerMsg("/" + entry.command);
                currentIndex++;
            }
            case ASDON -> {
                if (antiStuck != null && !antiStuck.isActive()) antiStuck.toggle();
                currentIndex++;
            }
            case ASDOFF -> {
                if (antiStuck != null && antiStuck.isActive()) antiStuck.toggle();
                currentIndex++;
            }
        }
    }


    private void handleFlyOn() {
        ChatUtils.sendPlayerMsg("/fly enable");
        flyTickCounter = 0;
        flyRequested = true;
        info("🚀 Gui lenh /fly enable...");
    }

    private void handleFlyOnTick() {
        flyTickCounter++;

        if (flyTickCounter == 2 || flyTickCounter == 6) mc.options.jumpKey.setPressed(true);
        else if (flyTickCounter == 3 || flyTickCounter == 7) mc.options.jumpKey.setPressed(false);

        if (mc.player.getAbilities().flying) {
            flyRequested = false;
            currentIndex++;
            info("🛫 Da bay thanh cong.");
        } else if (flyTickCounter > 20) {
            ChatUtils.sendPlayerMsg("/fly enable");
            flyTickCounter = 0;
            info("🔁 Gui lai lenh /fly.");
        }
    }

    private void handleFlyOff() {
        ChatUtils.sendPlayerMsg("/fly disable");
        waitingFlyOffLanding = true;
        currentIndex++;
        info("🪂 Gui lenh /fly disable, cho tiep dat...");
    }

    private void handlePos(WaypointEntry entry) {
        Vec3d target = entry.toVec3d();
        if (target == null) return;

        Vec3d pos = mc.player.getPos();
        if (pos.distanceTo(target) < reachRange.get()) {
            currentIndex++;
            reachedPreviousWaypoint = true;
        } else {
            reachedPreviousWaypoint = false;
            Vec3d motion = target.subtract(pos).normalize().multiply(0.6);
            mc.player.setVelocity(motion);
        }
    }

    private void handleGoto(WaypointEntry entry) {
        Vec3d target = entry.toVec3d();
        if (target == null) return;

        Vec3d pos = mc.player.getPos();
        if (pos.distanceTo(target) <= 1.0) {
            currentIndex++;
            gotoTickCounter = 0;
        } else {
            gotoTickCounter++;
            if (gotoTickCounter >= 20) {
                gotoTickCounter = 0;
                ChatUtils.sendPlayerMsg(String.format("#goto %.1f %.1f %.1f", target.x, target.y, target.z));
            }
        }
    }

    private void handleDelay(WaypointEntry entry) {
        if (!reachedPreviousWaypoint) return;
        if (delayTicks == 0) {
            delayTicks = entry.delay * 20;
        }

        delayTicks--;
        if (delayTicks <= 0) {
            delayTicks = 0;
            currentIndex++;
            reachedPreviousWaypoint = true;
        }
    }

    private void finishSequence() {
        if (enableWaypointFly.get()) {
            WaypointFly wf = Modules.get().get(WaypointFly.class);
            if (!wf.isActive()) wf.toggle();
        }

        toggle(); // Tat ReturnToStart sau cung
    }


    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (isActive()) {
            info("🔌 Disconnect - dung ReturnToStart.");
            toggle();
        }
    }

    private Vec3d parsePosition(String s) {
        try {
            String[] parts = s.trim().split(" ");
            if (parts.length != 3) return null;
            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            double z = Double.parseDouble(parts[2]);
            return new Vec3d(x, y, z);
        } catch (Exception e) {
            return null;
        }
    }

    private void startSequence() {
        if (antiStuck == null)
            antiStuck = Modules.get().get(AntiStuckDetector.class);

        entries.clear();
        currentIndex = 0;
        delayTicks = 0;
        gotoTickCounter = 0;
        flyTickCounter = 0;
        flyRequested = false;
        waitingFlyOffLanding = false;
        reachedPreviousWaypoint = true;

        for (String line : actions.get()) {
            try {
                entries.add(new WaypointEntry(line));
            } catch (Exception e) {
                warning("❌ Loi khi phan tich hanh dong: " + line);
            }
        }

        meteordevelopment.meteorclient.MeteorClient.EVENT_BUS.subscribe(this);
    }


}
