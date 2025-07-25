package zgoly.meteorist.modules.tests;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.events.world.TickEvent;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import zgoly.meteorist.Meteorist;

import java.util.*;

public class SlotClickTest extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<String>> layoutSetting = sgGeneral.add(new StringListSetting.Builder()
            .name("layout")
            .description("slot|item_id|count. Ví dụ: 81|minecraft:paper|1")
            .defaultValue(List.of("81|minecraft:paper|1"))
            .build()
    );

    private boolean ready = false;
    private int layoutIndex = 0;
    private boolean waiting = false;
    private int waitTicks = 0;
    private enum Step { IDLE, PICK_ONE, PLACE_ONE, RETURN_REMAINDER }
    private Step step = Step.IDLE;
    private int chestSlot = -1;
    private int invSlot = -1;

    public SlotClickTest() {
        super(Meteorist.Custom, "slot-click-test", "Tự động lấy item từ chest xuống inventory.");
    }

    @Override
    public void onActivate() {
        ready = false;
        layoutIndex = 0;
        waiting = false;
        waitTicks = 0;
        step = Step.IDLE;
        info("⏳ Đợi mở chest hoặc vault...");
    }

    @Override
    public void onDeactivate() {
        layoutIndex = 0;
        waiting = false;
        waitTicks = 0;
        step = Step.IDLE;
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (event.screen == null) return;

        String title = event.screen.getTitle().getString().toLowerCase();
        if (title.contains("chest") || title.contains("vault")) {
            info("📥 Đã mở GUI chứa item: \"" + title + "\".");
            ready = true;
            layoutIndex = 0;
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!ready || mc.player == null || mc.currentScreen == null) return;
        if (!(mc.currentScreen instanceof HandledScreen<?> screen)) return;

        ScreenHandler handler = screen.getScreenHandler();

        if (waiting) {
            waitTicks--;
            if (waitTicks <= 0) waiting = false;
            else return;
        }

        if (step == Step.IDLE) {
            if (layoutIndex >= layoutSetting.get().size()) {
                info("✅ Hoàn tất.");
                mc.player.closeHandledScreen();
                toggle();
                return;
            }

            String[] parts = layoutSetting.get().get(layoutIndex).split("\\|");
            try {
                invSlot = Integer.parseInt(parts[0].trim());
                String itemId = parts[1].trim();
                int count = Integer.parseInt(parts[2].trim());

                if (count <= 0) {
                    layoutIndex++;
                    return;
                }

                // Tìm item phù hợp trong chest (slot 0–53)
                for (Slot slot : handler.slots) {
                    int id = slot.getIndex();
                    if (id < 0 || id > 53) continue;

                    ItemStack stack = slot.getStack();
                    if (stack.isEmpty()) continue;

                    String foundId = Registries.ITEM.getId(stack.getItem()).toString();
                    if (foundId.equals(itemId) && stack.getCount() >= count) {
                        chestSlot = slot.id;
                        step = Step.PICK_ONE;
                        return;
                    }
                }

                warning("❌ Không tìm thấy item " + itemId + " trong chest.");
                layoutIndex++;
                waitTicks = 2;
                waiting = true;

            } catch (Exception e) {
                error("❌ Lỗi layout dòng " + layoutIndex + ": " + e.getMessage());
                layoutIndex++;
            }
            return;
        }

        // Nếu tay đang cầm item, tiếp tục xử lý theo step
        switch (step) {
            case PICK_ONE -> {
                // Click phải để lấy 1 item
                mc.interactionManager.clickSlot(handler.syncId, chestSlot, 1, SlotActionType.PICKUP, mc.player);
                step = Step.PLACE_ONE;
                waitTicks = 2;
                waiting = true;
            }
            case PLACE_ONE -> {
                mc.interactionManager.clickSlot(handler.syncId, invSlot, 1, SlotActionType.PICKUP, mc.player);
                step = Step.RETURN_REMAINDER;
                waitTicks = 2;
                waiting = true;
            }
            case RETURN_REMAINDER -> {
                if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
                    mc.interactionManager.clickSlot(handler.syncId, chestSlot, 0, SlotActionType.PICKUP, mc.player);
                }
                step = Step.IDLE;
                layoutIndex++;
                waitTicks = 2;
                waiting = true;
            }
        }
    }
}
