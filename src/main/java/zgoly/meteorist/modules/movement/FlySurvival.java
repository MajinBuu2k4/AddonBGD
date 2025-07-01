package zgoly.meteorist.modules.movement;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import zgoly.meteorist.Meteorist;

public class FlySurvival extends Module {

    public FlySurvival() {
        super(Meteorist.Custom, "fly", "Bay trong chế độ Survival như Creative nhưng vẫn chịu sát thương khi rơi.");
    }

    @Override
    public void onActivate() {
        if (mc.player == null || mc.interactionManager == null) return;

        // Không kích hoạt nếu đang ở chế độ Creative hoặc Spectator
        if (mc.player.isCreative() || mc.player.isSpectator()) {
            info("Bạn đang ở chế độ Creative/Spectator. Không cần bật Fly.");
            toggle(); // tự tắt module nếu không hợp lệ
            return;
        }

        mc.player.getAbilities().allowFlying = true;
        mc.player.sendAbilitiesUpdate();
    }

    @Override
    public void onDeactivate() {
        if (mc.player == null) return;

        mc.player.getAbilities().flying = false;
        mc.player.getAbilities().allowFlying = false;
        mc.player.sendAbilitiesUpdate();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        // Nếu đang ở Creative/Spectator thì tắt module
        if (mc.player.isCreative() || mc.player.isSpectator()) {
            if (isActive()) {
                info("Chế độ bay tắt do bạn đang ở Creative hoặc Spectator.");
                toggle();
            }
        }
    }
}
