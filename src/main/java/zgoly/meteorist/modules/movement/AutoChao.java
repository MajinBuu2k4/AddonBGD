package zgoly.meteorist.modules.movement;

import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import zgoly.meteorist.Meteorist;
import zgoly.meteorist.modules.movement.LicenseProtectedModule;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class AutoChao extends LicenseProtectedModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> autoChat = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-chao")
        .description("Tu dong chao menber moi")
        .defaultValue(true)
        .build()
    );

    private final Pattern pattern = Pattern.compile("Chào mừng \\[ (.*?) ] lần đầu tiên đến");

    private boolean isActivated = false;

    public AutoChao() {
        super(Meteorist.Custom, "auto-chao", "Tu dong chao menber moi.");
    }

    @Override
    public void onActivate() {
        super.onActivate(); // ✅ Kiểm tra license
        if (!isActive()) return;

        isActivated = true;
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        if (!isActivated || !autoChat.get()) return;

        String msg = event.getMessage().getString();
        Matcher matcher = pattern.matcher(msg);
        if (matcher.find()) {
            String playerName = matcher.group(1);
            ChatUtils.sendPlayerMsg("Xin chào bạn " + playerName);
        }
    }

    // ✅ Kiểm tra giấy phép từ file ngrok.lic

}
