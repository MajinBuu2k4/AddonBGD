package zgoly.meteorist.modules.WaypointFly;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import zgoly.meteorist.Meteorist;

public class AntiStuckDetector extends Module {
    private static final int DEFAULT_TICKS = 200;
    private int stuckTicks = 0;
    private Vec3d lastPos = null;
    private int maxTicksStuck = DEFAULT_TICKS;

    public AntiStuckDetector() {
        super(Meteorist.Custom, "anti-stuck", "Auto disconnect");
    }

    @Override
    public void onActivate() {
        stuckTicks = 0;
        lastPos = null;
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @Override
    public void onDeactivate() {
        MeteorClient.EVENT_BUS.unsubscribe(this);
    }

    public void setMaxTicksStuck(int ticks) {
        this.maxTicksStuck = ticks;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null) return;

        Vec3d currentPos = mc.player.getPos();
        if (lastPos != null) {
            double distance = currentPos.distanceTo(lastPos);
            if (distance < 0.1) {
                stuckTicks++;
                if (stuckTicks >= maxTicksStuck) {
                    disconnect("🚨 Bi ket qua lau!");
                }
            } else {
                stuckTicks = 0;
            }
        }
        lastPos = currentPos;
    }

    @EventHandler
    private void onDeathScreen(OpenScreenEvent event) {
        if (mc.player == null) return;
        if (event.screen instanceof DeathScreen) {
            disconnect("💀 Ban da chet!");
        }
    }

    private void disconnect(String message) {
        if (mc.player != null && mc.player.networkHandler != null) {
            mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(Text.of(message)));
        }
        toggle(); // Tu tat module
    }

    // Cho phep bat/tat thu cong tu WaypointFly
    public void enableExternally() {
        if (!this.isActive()) this.toggle();
    }

    public void disableExternally() {
        if (this.isActive()) this.toggle();
    }
}
