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

import zgoly.meteorist.modules.movement.LicenseProtectedModule;

public class AutoQuaAFK extends LicenseProtectedModule {
    public static AutoQuaAFK INSTANCE;

    private final Setting<String> khuAFK = settings.getDefaultGroup()
            .add(new StringSetting.Builder()
                    .name("khuafk")
                    .description("Lenh de den khu AFK")
                    .defaultValue("/warp afk")
                    .build());

    private boolean warped = false;
    private long warpTime = -1;
    private long lastCheckTime = 0;

    private final BlockPos SPAWN_POS = new BlockPos(-1251, 161, 614);

    public AutoQuaAFK() {
        super(Meteorist.Custom, "AutoQuaAFK", "Tu dong di den khu AFK sau khi login.");
        INSTANCE = this;
    }

    @Override
    public void onActivate() {
        super.onActivate(); // ✅ Kiểm tra license
        if (!isActive()) return; // ✅ Nếu không hợp lệ thì không chạy tiếp

        warped = false;
        warpTime = -1;
        lastCheckTime = System.currentTimeMillis();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;

        // Nếu chưa warp và đang ở đúng vị trí spawn
        if (!warped && isAtSpawn()) {
            ChatUtils.sendPlayerMsg(khuAFK.get());
            warped = true;
            warpTime = System.currentTimeMillis();
            return;
        }

        // Sau khi gửi lệnh warp 1s thì bật GotoBaritone để đi đến khu AFK
        if (warped && warpTime > 0 && System.currentTimeMillis() - warpTime >= 1000) {
            Module gotoBaritoneModule = Modules.get().get(GotoBaritone.class);
            if (gotoBaritoneModule != null && !gotoBaritoneModule.isActive()) {
                gotoBaritoneModule.toggle();  // Bật GotoBaritone
                info("✅ Bật GotoBaritone để di chuyển đến khu AFK.");
            }
            warpTime = -1; // chỉ chạy 1 lần
        }

        // Kiểm tra mỗi 10 giây
        if (System.currentTimeMillis() - lastCheckTime >= 10_000) {
            lastCheckTime = System.currentTimeMillis();

            if (checkConditionMet()) {
                info("✅ Da den khu AFK.");
            } else {
                warning("❌ Chua den khu AFK!");
            }
        }
    }

    @EventHandler
    private void onReceiveMsg(ReceiveMessageEvent event) {
        Text msg = event.getMessage();
        if (msg != null && msg.getString().contains("MajinBuu2k4 Đã tham gia máy chủ!")) {
            ChatUtils.sendPlayerMsg(khuAFK.get());
            warped = true;
            warpTime = System.currentTimeMillis();
        }
    }

    private boolean isAtSpawn() {
        BlockPos pos = mc.player.getBlockPos();
        return pos.equals(SPAWN_POS);
    }

    private boolean checkConditionMet() {
        // Kiểm tra đã warp thành công
        return warped;
    }
}
