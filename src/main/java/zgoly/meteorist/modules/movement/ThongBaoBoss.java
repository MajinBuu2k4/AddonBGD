package zgoly.meteorist.modules.movement;


import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.network.Http;
import zgoly.meteorist.Meteorist;

import java.util.Timer;
import java.util.TimerTask;


public class ThongBaoBoss extends LicenseProtectedModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // Thời gian hẹn giờ (phút)
    private final Setting<Integer> interval = sgGeneral.add(new IntSetting.Builder()
            .name("interval-minutes")
            .description("Khoang thoi gian moi thong bao (phút).")
            .defaultValue(15)
            .min(1)
            .sliderMax(60)
            .build()
    );

    // Webhook Discord URL
    private final Setting<String> webhookUrl = sgGeneral.add(new StringSetting.Builder()
            .name("webhook-url")
            .description("URL Webhook Discord de gui thong bao discord.")
            .defaultValue("Dan webhook discord vao")
            .build()
    );

    private Timer timer;

    public ThongBaoBoss() {
        super(Meteorist.Custom, "thong-bao-boss", "Thong bao boss qua Discord Webhook.");
    }

    @Override
    public void onActivate() {
        super.onActivate(); // ✅ Kiểm tra license
        if (!isActive()) return; // ✅ Nếu không hợp lệ thì không chạy tiếp


        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendWebhook("Boss xuất hiện rồi!!!");
            }
        }, 0, interval.get() * 60 * 1000L);
        info("Boss đã bật, thông báo mỗi " + interval.get() + " phút.");
    }

    @Override
    public void onDeactivate() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        info("Boss đã tắt.");
    }

    private void sendWebhook(String content) {
        String url = webhookUrl.get();
        if (url == null || url.isEmpty()) {
            error("Webhook URL chưa được cấu hình.");
            return;
        }

        String json = "{\"content\": \"" + content + "\"}";

        try {
            Http.post(url)
                    .bodyJson(json)
                    .send();
            info("Đã gửi thông báo tới Discord.");
        } catch (Exception e) {
            error("Gửi webhook thất bại: " + e.getMessage());
        }
    }
}
