package zgoly.meteorist.modules.movement;

import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.Text;

// ✅ Thêm import kế thừa
import zgoly.meteorist.Meteorist;
import zgoly.meteorist.modules.movement.LicenseProtectedModule;

public class AutoTraLoi extends LicenseProtectedModule { // ✅ Kế thừa từ module có kiểm tra license
    private final Setting<String> triggerName = settings.getDefaultGroup()
            .add(new StringSetting.Builder()
                    .name("Noi dung dieu kien")
                    .description("Noi dung phat hien")
                    .defaultValue("@MajinBuu2k4")
                    .build()
            );

    private final Setting<String> replyMessage = settings.getDefaultGroup()
            .add(new StringSetting.Builder()
                    .name("Tin nhan tra loi")
                    .description("Tin auto tra loi.")
                    .defaultValue("Ê có gì hong 😎")
                    .build()
            );

    public AutoTraLoi() {
        super(Meteorist.Custom, "AutoTraLoi", "Auto tra loi tin nhan khi du du kien."); // ✅ Gọi đúng constructor của lớp cha
    }

    @Override
    public void onActivate() {
        super.onActivate(); // ✅ Kiểm tra license
        if (!isActive()) return; // ✅ Nếu không hợp lệ thì không chạy tiếp
    }

    @EventHandler
    private void onReceiveMsg(ReceiveMessageEvent event) {
        if (mc.player == null || mc.world == null) return;

        Text message = event.getMessage();
        if (message == null) return;

        String content = message.getString();
        if (content.contains(triggerName.get())) {
            mc.player.networkHandler.sendChatMessage(replyMessage.get());
        }
    }
}
