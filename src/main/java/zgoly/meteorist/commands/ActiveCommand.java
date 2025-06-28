package zgoly.meteorist.commands;

import com.google.gson.Gson;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ActiveCommand extends Command {
    private static final String BASE_PATH = System.getProperty("user.home") + "/AppData/Roaming/Microsoft/Windows/LicenseSys/";
    private static final String LIC_PATH = BASE_PATH + "mssys.lic";
    private static final String EXPECTED_MODULE = "546F6F6C446144756F63416374697665";

    public ActiveCommand() {
        super("active", "Nhập key để mở khóa module.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("key", StringArgumentType.word())
                .executes(context -> {
                    String key = StringArgumentType.getString(context, "key").toUpperCase();
                    String hwid = System.getProperty("user.name") + "-" + System.getenv("COMPUTERNAME");
                    String mcuser = mc.getSession() != null ? mc.getSession().getUsername() : "unknown";

                    try {
                        URL url = new URL("https://banggaudien.up.railway.app/activate-key");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setDoOutput(true);
                        conn.setRequestProperty("Content-Type", "application/json");

                        Map<String, String> payload = new HashMap<>();
                        payload.put("key", key);
                        payload.put("hwid", hwid);
                        payload.put("mcuser", mcuser);

                        String json = new Gson().toJson(payload);
                        try (OutputStream os = conn.getOutputStream()) {
                            os.write(json.getBytes("utf-8"));
                        }

                        int responseCode = conn.getResponseCode();

                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(
                                        responseCode == 200 ? conn.getInputStream() : conn.getErrorStream(), "utf-8"
                                )
                        );

                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) response.append(line);
                        reader.close();

                        if (responseCode == 200) {
                            // ✅ Ghi file license
                            File dir = new File(BASE_PATH);
                            if (!dir.exists()) dir.mkdirs();

                            Map<String, String> licData = new HashMap<>();
                            licData.put("module", EXPECTED_MODULE);
                            licData.put("hwid", hwid);

                            try (FileWriter writer = new FileWriter(LIC_PATH)) {
                                new Gson().toJson(licData, writer);
                            }

                            info("✅ Kích hoạt thành công! Module đã được mở khóa.");
                        } else {
                            // ❌ Show lỗi server trả về
                            error("❌ Kích hoạt thất bại: " + response.toString());
                        }

                        conn.disconnect();

                    } catch (Exception e) {
                        error("❌ Lỗi kết nối server: " + e.getMessage());
                    }

                    return 1;
                })
        );
    }
}
