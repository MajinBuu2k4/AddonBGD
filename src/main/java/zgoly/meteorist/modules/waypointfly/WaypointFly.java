package zgoly.meteorist.modules.WaypointFly;

import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;
import zgoly.meteorist.Meteorist;
import meteordevelopment.meteorclient.systems.modules.Modules;
import zgoly.meteorist.modules.movement.LicenseProtectedModule;

import java.util.List;
import java.util.ArrayList;

public class WaypointFly extends LicenseProtectedModule {
    public final SettingGroup sgGeneral = settings.getDefaultGroup();
    public final Setting<List<String>> waypointStrings = sgGeneral.add(new StringListSetting.Builder()
            .name("waypoints")
            .description("Danh sach cac waypoint.")
            .defaultValue(new ArrayList<>())
            .build()
    );

    public final Setting<Boolean> autoEnableFly = sgGeneral.add(new BoolSetting.Builder()
            .name("auto-enable-fly")
            .description("Tu dong gui /fly enable khi bat module.")
            .defaultValue(true)
            .build()
    );

    public final Setting<Double> reachRange = sgGeneral.add(new DoubleSetting.Builder()
            .name("waypoint-reach-range")
            .description("Khoang cach toi da tinh la da den waypoint.")
            .defaultValue(3)
            .min(0.1)
            .sliderMax(10)
            .build()
    );

    public final Setting<Integer> gotoResendDelay = sgGeneral.add(new IntSetting.Builder()
            .name("goto-resend-delay")
            .description("So tick (1/20s) cho moi lan gui lai lenh #goto.")
            .defaultValue(20) // 1 giây
            .min(1)
            .sliderMax(100)
            .build()
    );


    public final Setting<Boolean> loop = sgGeneral.add(new BoolSetting.Builder()
            .name("loop")
            .description("Lap lai waypoint sau khi hoan thanh.")
            .defaultValue(false)
            .build()
    );

    public final Setting<Boolean> disableOnDisconnect = sgGeneral.add(new BoolSetting.Builder()
            .name("disable-on-disconnect")
            .description("Tu dong tat module khi disconnect khoi server.")
            .defaultValue(true)
            .build()
    );

    public final Setting<Boolean> useFlySpeed = sgGeneral.add(new BoolSetting.Builder()
            .name("use-fly-speed")
            .description("Bat toc do bay tuy chinh.")
            .defaultValue(false)
            .build()
    );

    public final Setting<Double> flySpeed = sgGeneral.add(new DoubleSetting.Builder()
            .name("fly-speed")
            .description("Toc do bay giua cac waypoint.")
            .defaultValue(1.0)
            .min(0.1)
            .sliderMax(5)
            .visible(() -> useFlySpeed.get())
            .build()
    );

    private AntiStuckDetector antiStuck;

    private int currentIndex = 0;
    private int delayTicks = 0;
    private int flyTicks = 0;
    private int gotoTickCounter = 0;

    private boolean waitingToFly = false;
    private boolean waitingForLanding = false;
    private boolean reachedPreviousWaypoint = true;
    private List<WaypointEntry> entries = new ArrayList<>();

    public WaypointFly() {
        super(Meteorist.Custom, "waypoint-fly-v1", "Auto fly mine v1.");
    }

    @Override
    public void onActivate() {
        super.onActivate(); // ✅ Kiểm tra license
        if (!isActive()) return;
        if (mc.player == null) return;

        if (antiStuck == null) {
            antiStuck = Modules.get().get(AntiStuckDetector.class);
            if (antiStuck == null) warning("⚠️ Khong tim thay module AntiStuckDetector.");
        }

        if (autoEnableFly.get()) {
            ChatUtils.sendPlayerMsg("/fly enable");
            waitingToFly = true;
            info("✈️ Tu dong bat che do bay (/fly).");
        } else {
            waitingToFly = false; // Không chờ bay nữa
            info("⚡ Bat dau waypoint ngay (khong bat /fly).");
        }
        flyTicks = 0;
        currentIndex = 0;
        delayTicks = 0;
        reachedPreviousWaypoint = true;

        entries.clear();

        for (String line : waypointStrings.get()) {
            try {
                WaypointEntry entry = new WaypointEntry(line);
                entries.add(entry);
            } catch (Exception e) {
                warning("❌ Loi khi phan tich waypoint: " + line);
            }
        }

        meteordevelopment.meteorclient.MeteorClient.EVENT_BUS.subscribe(this);
    }

    @Override
    public void onDeactivate() {
        if (mc.player != null) mc.player.setVelocity(Vec3d.ZERO);
        meteordevelopment.meteorclient.MeteorClient.EVENT_BUS.unsubscribe(this);
        waitingToFly = false;
        waitingForLanding = false;
        flyTicks = 0;
        delayTicks = 0;
        gotoTickCounter = 0;
        currentIndex = 0;
        if (antiStuck != null && antiStuck.isActive()) antiStuck.toggle();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!isActive() || mc.player == null) return;

        if (entries == null || entries.isEmpty()) {
            info("⚠️ Khong co waypoint nao, tat module.");
            toggle();
            return;
        }

        if (currentIndex >= entries.size()) {
            if (loop.get()) {
                info("🔁 Lap lai waypoint tu dau.");
                currentIndex = 0;
                return;
            } else {
                toggle();
                info("✅ Da hoan thanh waypoint.");
                return;
            }
        }

        if (waitingToFly) {
            if (!autoEnableFly.get()) {
                waitingToFly = false; // Nếu không bật auto thì bỏ qua chờ bay
                return;
            }

            flyTicks++;
            if (flyTicks == 2 || flyTicks == 6) mc.options.jumpKey.setPressed(true);
            else if (flyTicks == 3 || flyTicks == 7) mc.options.jumpKey.setPressed(false);

            if (flyTicks > 20 && !mc.player.getAbilities().flying) {
                flyTicks = 0;
                ChatUtils.sendPlayerMsg("/fly enable");
                info("🔁 Gui lai lenh /fly.");
            }

            if (mc.player.getAbilities().flying) {
                waitingToFly = false;
                info("🛫 Da vao che do bay.");
            }

            return;
        }

        if (waitingForLanding) {
            if (mc.player.isOnGround()) {
                waitingForLanding = false;
                currentIndex++;
                reachedPreviousWaypoint = true;
                info("✅ Da tiep dat, tiep tuc waypoint.");
            }
            return;
        }

        WaypointEntry entry = entries.get(currentIndex);
        switch (entry.type) {
            case POS -> handlePos(entry);
            case GOTO -> handleGoto(entry);
            case DELAY -> handleDelay(entry);
            case FLYON -> {
                ChatUtils.sendPlayerMsg("/fly enable");
                waitingToFly = true;
                currentIndex++;
            }
            case FLYOFF -> {
                ChatUtils.sendPlayerMsg("/fly disable");
                waitingForLanding = true;
                info("🪂 Da tat che do bay, cho tiep dat...");
            }
            case COMMAND -> {
                ChatUtils.sendPlayerMsg("/" + entry.command);
                currentIndex++;
            }
            case ASDON -> {
                handleASDON(entry);
                currentIndex++;
            }
            case ASDOFF -> {
                handleASDOFF(entry);
                currentIndex++;
            }
        }
    }

    private void handleDelay(WaypointEntry entry) {
        if (!reachedPreviousWaypoint) return;

        if (delayTicks == 0) {
            delayTicks = entry.delay * 20;
            info("⏱️ Delay " + entry.delay + " giay.");
        }

        delayTicks--;
        if (delayTicks <= 0) {
            delayTicks = 0;
            currentIndex++;
            reachedPreviousWaypoint = true;
        }
    }

    private void handlePos(WaypointEntry entry) {
        Vec3d target = entry.toVec3d();
        if (target == null) return;

        Vec3d playerPos = mc.player.getPos();
        double dist = playerPos.distanceTo(target);

        if (dist < reachRange.get()) {
            currentIndex++;
            reachedPreviousWaypoint = true;
        } else {
            reachedPreviousWaypoint = false;
            double speed = useFlySpeed.get() ? flySpeed.get() : 0.6;
            Vec3d motion = target.subtract(playerPos).normalize().multiply(speed);
            mc.player.setVelocity(motion);
        }
    }

    private void handleGoto(WaypointEntry entry) {
        Vec3d target = entry.toVec3d();
        if (target == null) return;

        Vec3d playerPos = mc.player.getPos();
        double distance = playerPos.distanceTo(target);

        if (distance <= reachRange.get()) {
            info("📍 Da den GOTO: " + target);
            currentIndex++;
            gotoTickCounter = 0;
            return;
        }

        gotoTickCounter++;
        if (gotoTickCounter >= gotoResendDelay.get()) {
            gotoTickCounter = 0;
            String baritoneCmd = String.format("#goto %.1f %.1f %.1f", target.x, target.y, target.z);
            ChatUtils.sendPlayerMsg(baritoneCmd);
            info("➡️ Gui lai Baritone: " + baritoneCmd);
        }
    }

    private void handleASDON(WaypointEntry entry) {
        if (antiStuck == null) antiStuck = Modules.get().get(AntiStuckDetector.class);

        if (antiStuck != null && !antiStuck.isActive()) {
            antiStuck.toggle();
            info("🟢 Da bat AntiStuckDetector.");
        } else if (antiStuck == null) {
            warning("⚠️ Khong tim thay AntiStuckDetector de bat.");
        }
    }

    private void handleASDOFF(WaypointEntry entry) {
        if (antiStuck == null) antiStuck = Modules.get().get(AntiStuckDetector.class);

        if (antiStuck != null && antiStuck.isActive()) {
            antiStuck.toggle();
            info("🔴 Da tat AntiStuckDetector.");
        } else if (antiStuck == null) {
            warning("⚠️ Khong tim thay AntiStuckDetector de tat.");
        }
    }

    public void addWaypoint(String wp) {
        List<String> list = new ArrayList<>(waypointStrings.get());
        list.add(wp);
        waypointStrings.set(list);
        info("📌 Da them: " + wp);
    }

    public void clearWaypoints() {
        waypointStrings.set(new ArrayList<>());
        info("🧹 Da xoa tat ca waypoint.");
    }

    public List<String> getWaypoints() {
        return waypointStrings.get();
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (!isActive()) return;
        if (disableOnDisconnect.get()) {
            info("🔌 Da disconnect khoi server, tat WaypointFly.");
            toggle();
        } else {
            info("🔌 Da disconnect khoi server, nhung khong tat module.");
        }
    }
}
