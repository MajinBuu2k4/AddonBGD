package zgoly.meteorist.Network;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class UseModuleReporter {
    private boolean sent = false;
    private long lastCheck = 0;

    public UseModuleReporter() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (sent) return;

        if (System.currentTimeMillis() - lastCheck < 5000) return;
        lastCheck = System.currentTimeMillis();

        new Thread(() -> {
            try {
                String username = MeteorClient.mc.getSession() != null
                        ? MeteorClient.mc.getSession().getUsername()
                        : "unknown";

                String hwid = getHWID();
                List<String> activeModules = Modules.get().getActive().stream()
                        .map(m -> m.name)
                        .collect(Collectors.toList());

                ReportData report = new ReportData(username, hwid, activeModules);
                String json = new Gson().toJson(report);

                URL url = new URL("https://ldhdtghrvijamxhukcxu.supabase.co/rest/v1/use_module");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImxkaGR0Z2hydmlqYW14aHVrY3h1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTEwNzMzNTQsImV4cCI6MjA2NjY0OTM1NH0.zpJsvWxQ1PAnikU53Yzu2DZutvCJgIqdPYM6TqukA-g");
                conn.setRequestProperty("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImxkaGR0Z2hydmlqYW14aHVrY3h1Iiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc1MTA3MzM1NCwiZXhwIjoyMDY2NjQ5MzU0fQ.a1GToBO0lVcNtIVWF4U05b7bWQaOOCgd_A23ijZsc7I");
                conn.setRequestProperty("Prefer", "return=minimal");

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes("utf-8"));
                }

                conn.getResponseCode();
                conn.disconnect();

                sent = true;
            } catch (Exception e) {
                e.printStackTrace(); // Bạn có thể ghi log nếu cần
            }
        }).start();
    }

    // Tạo HWID đơn giản
    private String getHWID() {
        try {
            String data = System.getProperty("os.name") +
                    System.getProperty("os.arch") +
                    System.getProperty("user.name") +
                    System.getenv("PROCESSOR_IDENTIFIER");

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data.getBytes("UTF-8"));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            return "unknown_hwid";
        }
    }

    // Dữ liệu gửi lên Supabase
    static class ReportData {
        @SerializedName("username") String username;
        @SerializedName("hwid") String hwid;
        @SerializedName("modules") List<String> modules;

        ReportData(String username, String hwid, List<String> modules) {
            this.username = username;
            this.hwid = hwid;
            this.modules = modules;
        }
    }
}
