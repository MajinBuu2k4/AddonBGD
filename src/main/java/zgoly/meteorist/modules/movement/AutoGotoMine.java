package zgoly.meteorist.modules.movement;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import zgoly.meteorist.Meteorist;

// ✅ Sử dụng lớp bảo vệ bằng license
import zgoly.meteorist.modules.movement.LicenseProtectedModule;

import java.util.List;

public class AutoGotoMine extends LicenseProtectedModule { // ✅ Kế thừa LicenseProtectedModule
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<String>> coordinates = sgGeneral.add(new StringListSetting.Builder()
            .name("coordinates")
            .description("Danh sach toa do: x y z|delay")
            .defaultValue(List.of("-252 0 134|20", "-248 0 134|20", "-430 1 76|20", "-433 1 76|20"))
            .build());

    private final Setting<Boolean> skipOnDamage = sgGeneral.add(new BoolSetting.Builder()
            .name("skip-on-damage")
            .description("Bi tan cong bo qua delay")
            .defaultValue(true)
            .build());

    private int index = 0;
    private int timer = 0;
    private boolean skipDelay = false;

    public AutoGotoMine() {
        // ✅ Gán vào tab GUI Custom
        super(Meteorist.Custom, "auto-goto-mine", "Auto Baritone bi mob tan cong bo delay");
    }

    @Override
    public void onActivate() {
        super.onActivate(); // ✅ Kiểm tra license
        if (!isActive()) return; // ✅ Nếu không hợp lệ thì không chạy tiếp

        index = 0;
        timer = 0;
        skipDelay = false;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || coordinates.get().isEmpty()) return;

        // Nếu bị đánh và bật tính năng skip delay
        if (skipOnDamage.get() && mc.player.hurtTime > 0) {
            skipDelay = true;
        }

        if (timer <= 0 || skipDelay) {
            if (index >= coordinates.get().size()) index = 0;

            String raw = coordinates.get().get(index);
            String[] parts = raw.split("\\|");
            String coord = parts[0];
            int delaySec = 5;

            if (parts.length > 1) {
                try {
                    delaySec = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException e) {
                    warning("Delay không hợp lệ tại dòng: " + raw + ". Dùng mặc định 5s.");
                }
            }

            ChatUtils.sendPlayerMsg("#goto " + coord);
            info("Di chuyển đến: " + coord + " (delay " + delaySec + "s)");
            timer = delaySec * 20;
            index++;
            skipDelay = false;
        } else {
            timer--;
        }
    }
}
