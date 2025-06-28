package zgoly.meteorist.modules.movement;

import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import zgoly.meteorist.Meteorist;

public class AutoClickLoginCum extends LicenseProtectedModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> autoDisable = sgGeneral.add(new BoolSetting.Builder()
            .name("tu-tat-khi-bind")
            .description("Tu tat khi (bind).")
            .defaultValue(true)
            .build()
    );

    private static boolean clickedFirstGui = false;
    private static boolean clickedSecondGui = false;

    private long lastRun = 0;
    private final long delay = 2_000; // 10s delay giữa mỗi vòng lặp
    private long activateTime = 0;
    private boolean shouldAutoDisable = false;

    public AutoClickLoginCum() {
        super(Meteorist.Custom, "AutoClickLoginCum", "Auto Click Login Cum.");
    }

    @Override
    public void onActivate() {
        super.onActivate(); // ✅ Kiểm tra license
        if (!isActive()) return; // ✅ Nếu không hợp lệ thì không chạy tiếp

        activateTime = System.currentTimeMillis();
        lastRun = activateTime - delay; // Cho chạy lần đầu ngay
        shouldAutoDisable = autoDisable.get() && isLikelyBindActivation();
    }

    // Heuristic: nếu module được kích hoạt trong vòng 100ms trở lại, giả định là do bind
    private boolean isLikelyBindActivation() {
        return System.currentTimeMillis() - activateTime < 100;
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (mc.player == null || mc.world == null) return;

        long now = System.currentTimeMillis();
        if (now - lastRun < delay) return;

        // Kiểm tra slot 5 có đồng hồ không
        if (mc.player.getInventory().getStack(4).getItem() != Items.CLOCK) {
            lastRun = now;
            return;
        }

        // Chọn slot 5 và dùng item
        mc.player.getInventory().selectedSlot = 4;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);

        clickedFirstGui = false;
        clickedSecondGui = false;

        lastRun = now;
    }

    @EventHandler
    private void onGui(Render2DEvent event) {
        if (!(mc.currentScreen instanceof GenericContainerScreen)) return;

        ScreenHandler handler = ((GenericContainerScreen) mc.currentScreen).getScreenHandler();

        // Click slot 22
        if (!clickedFirstGui && isClickable(handler, 22)) {
            mc.interactionManager.clickSlot(handler.syncId, 22, 0, SlotActionType.PICKUP, mc.player);
            clickedFirstGui = true;
            return;
        }

        // Click slot 34
        if (clickedFirstGui && !clickedSecondGui && isClickable(handler, 34)) {
            mc.interactionManager.clickSlot(handler.syncId, 34, 0, SlotActionType.PICKUP, mc.player);
            clickedSecondGui = true;


        }
    }

    private boolean isClickable(ScreenHandler handler, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= handler.slots.size()) return false;
        Slot slot = handler.getSlot(slotIndex);
        return slot != null && slot.hasStack();
    }
}
