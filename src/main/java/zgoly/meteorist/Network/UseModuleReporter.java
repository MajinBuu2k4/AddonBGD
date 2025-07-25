package zgoly.meteorist.Network;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class UseModuleReporter {
    private final Gson gson = new Gson();
    private long lastCheck = 0;

    public UseModuleReporter() {
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (System.currentTimeMillis() - lastCheck < 10000) return; // 10s cooldown
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

                // Gửi dữ liệu report
                ReportData report = new ReportData(username, hwid, activeModules);
                String json = gson.toJson(report);

                HttpURLConnection postConn = (HttpURLConnection) new URL("https://module.michaelphucs.xyz/api/report").openConnection();
                postConn.setRequestMethod("POST");
                postConn.setDoOutput(true);
                postConn.setRequestProperty("Content-Type", "application/json");

                try (OutputStream os = postConn.getOutputStream()) {
                    os.write(json.getBytes("utf-8"));
                }
                postConn.getResponseCode();
                postConn.disconnect();

                // Đọc lệnh từ server (gửi từ /quanly)
                URL getUrl = new URL("https://module.michaelphucs.xyz/data/sendserver/" + username + ".json");
                HttpURLConnection getConn = (HttpURLConnection) getUrl.openConnection();
                getConn.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(getConn.getInputStream()));
                Map<String, String> controlMap = gson.fromJson(in, new TypeToken<HashMap<String, String>>(){}.getType());
                in.close();
                getConn.disconnect();

                // Nếu có lệnh → xử lý + chuẩn bị dữ liệu sau xử lý
                boolean changed = false;
                Map<String, String> remainingCommands = new HashMap<>();

                for (Map.Entry<String, String> entry : controlMap.entrySet()) {
                    String modName = entry.getKey();
                    String status = entry.getValue();

                    Module module = Modules.get().get(modName);
                    if (module != null) {
                        if (status.equalsIgnoreCase("OFF") && module.isActive()) {
                            module.toggle();
                            changed = true;
                        } else if (status.equalsIgnoreCase("ON") && !module.isActive()) {
                            module.toggle();
                            changed = true;
                        }
                        // Không thêm vào remainingCommands nếu đã xử lý
                    } else {
                        // Giữ lại lệnh nếu module không tồn tại
                        remainingCommands.put(modName, status);
                    }
                }

                // Nếu còn lệnh chưa xử lý → cập nhật lại file
                if (changed || !remainingCommands.isEmpty()) {
                    URL clearUrl = new URL("https://module.michaelphucs.xyz/api/clear-command");
                    HttpURLConnection clearConn = (HttpURLConnection) clearUrl.openConnection();
                    clearConn.setRequestMethod("POST");
                    clearConn.setDoOutput(true);
                    clearConn.setRequestProperty("Content-Type", "application/json");

                    Map<String, Object> payload = new HashMap<>();
                    payload.put("username", username);
                    payload.put("commands", remainingCommands);

                    String clearJson = gson.toJson(payload);
                    try (OutputStream os = clearConn.getOutputStream()) {
                        os.write(clearJson.getBytes("utf-8"));
                    }

                    clearConn.getResponseCode();
                    clearConn.disconnect();
                }

            } catch (Exception e) {
                //e.printStackTrace(); // Bạn bật log nếu muốn
            }
        }).start();
    }

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

    static class ReportData {
        String username;
        String hwid;
        List<String> modules;

        ReportData(String username, String hwid, List<String> modules) {
            this.username = username;
            this.hwid = hwid;
            this.modules = modules;
        }
    }
}
