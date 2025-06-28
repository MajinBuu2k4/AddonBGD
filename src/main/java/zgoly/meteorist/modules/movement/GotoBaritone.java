package zgoly.meteorist.modules.movement;

import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.Text;
import zgoly.meteorist.Meteorist;

import zgoly.meteorist.modules.movement.LicenseProtectedModule;
public class GotoBaritone extends LicenseProtectedModule {
    public static GotoBaritone INSTANCE;

    private final Setting<String> xCoord = settings.getDefaultGroup()
            .add(new StringSetting.Builder()
                    .name("x")
                    .description("Nhap toa do X")
                    .defaultValue("0")
                    .build());

    private final Setting<String> yCoord = settings.getDefaultGroup()
            .add(new StringSetting.Builder()
                    .name("y")
                    .description("Nhap toa do Y")
                    .defaultValue("64")
                    .build());

    private final Setting<String> zCoord = settings.getDefaultGroup()
            .add(new StringSetting.Builder()
                    .name("z")
                    .description("Nhap toa do Z")
                    .defaultValue("0")
                    .build());

    private boolean moved = false;

    public GotoBaritone() {
        super(Meteorist.Custom, "GotoBaritone", "Tu dong di chuyen den toa do bang Baritone");
        INSTANCE = this;
    }

    @Override
    public void onActivate() {
        super.onActivate();
        if (!isActive()) return;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || moved) return;

        try {
            String x = xCoord.get();
            String y = yCoord.get();
            String z = zCoord.get();

            // Gửi lệnh #goto qua ChatUtils giống như trong AutoQuaAFK
            String command = "#goto " + x + " " + y + " " + z;
            ChatUtils.sendPlayerMsg(command);
            info("🚶 Dang di den: " + command);
            moved = true;
            toggle(); // Tắt module sau khi gửi lệnh
        } catch (Exception e) {
            warning("❌ Loi khi gui lenh #goto");
            toggle(); // Tắt module khi có lỗi
        }
    }

    @EventHandler
    private void onReceiveMsg(ReceiveMessageEvent event) {
        Text msg = event.getMessage();
        // Có thể thêm các điều kiện ở đây nếu muốn gửi lệnh khi nhận được tin nhắn đặc biệt
        // Ví dụ: Khi nhận tin nhắn từ server, thực hiện hành động gửi lệnh #goto
    }
}
