package zgoly.meteorist.modules.movement;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import zgoly.meteorist.Meteorist;

import zgoly.meteorist.modules.movement.LicenseProtectedModule;

public class LookAtOnce extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // Thay FloatSetting bằng DoubleSetting
    private final Setting<Double> yaw = sgGeneral.add(new DoubleSetting.Builder()
            .name("yaw")
            .description("Hướng ngang (0 = South, 90 = West, -90 = East, 180/-180 = North).")
            .defaultValue(0.0)
            .min(-180.0)
            .max(180.0)
            .sliderMax(180.0)
            .build()
    );

    private final Setting<Double> pitch = sgGeneral.add(new DoubleSetting.Builder()
            .name("pitch")
            .description("Hướng dọc (90 = nhìn xuống, -90 = nhìn lên, 0 = ngang).")
            .defaultValue(90.0)
            .min(-90.0)
            .max(90.0)
            .sliderMax(90.0)
            .build()
    );

    private boolean rotated = false;

    public LookAtOnce() {
        super(Meteorist.Custom, "look-at-once", "Xoay đầu đúng góc (yaw/pitch) một lần rồi tự tắt.");
    }

    @Override
    public void onActivate() {
        super.onActivate(); // ✅ Kiểm tra license
        if (!isActive()) return; // ✅ Nếu không hợp lệ thì không chạy tiếp
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        // Set hướng nhìn
        mc.player.setYaw(yaw.get().floatValue()); // Chuyển Double sang float
        mc.player.setPitch(pitch.get().floatValue()); // Chuyển Double sang float

        // Kiểm tra đã xoay đúng chưa
        if (!rotated) {
            float currentYaw = mc.player.getYaw();
            float currentPitch = mc.player.getPitch();

            // Kiểm tra độ lệch, nếu lệch quá ít thì dừng module
            if (Math.abs(currentYaw - yaw.get().floatValue()) < 0.01f && Math.abs(currentPitch - pitch.get().floatValue()) < 0.01f) {
                rotated = true;
                toggle(); // Tự tắt module
            }
        }
    }
}
