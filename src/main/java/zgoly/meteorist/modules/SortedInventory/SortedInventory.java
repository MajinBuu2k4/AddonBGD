package zgoly.meteorist.modules.SortedInventory;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import zgoly.meteorist.Meteorist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import com.google.gson.*;
import zgoly.meteorist.modules.movement.LicenseProtectedModule;

import java.util.*;

public class SortedInventory extends LicenseProtectedModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<String>> layoutSetting = sgGeneral.add(new StringListSetting.Builder()
            .name("layout")
            .description("slot|item_id|count. Vi du: 81|minecraft:paper|1")
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

    public SortedInventory() {
        super(Meteorist.Custom, "sorted-inventory", "Tu dong lay item tu vault theo layout dinh san.");
    }

    @Override
    public void onActivate() {
        super.onActivate(); // ✅ Kiem tra license
        if (!isActive()) return; // ✅ License hop le tiep tuc
        ready = false;
        layoutIndex = 0;
        waiting = false;
        waitTicks = 0;
        step = Step.IDLE;

        info("📨 Gui lenh mo Vault...");
        ChatUtils.sendPlayerMsg("/pv 2");
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

        // Phat hien Vault hoac Chest (Large Chest cung bao gom)
        if (title.contains("vault") || title.contains("chest")) {
            info("📥 Da mo GUI chua item: \"" + title + "\".");
            ready = true;
            layoutIndex = 0;

            // Log item trong chest hoac vault
            if (event.screen instanceof HandledScreen<?> screen) {
                ScreenHandler handler = screen.getScreenHandler();
                info("🔎 Danh sach item co trong GUI:");
                for (Slot slot : handler.slots) {
                    int id = slot.getIndex();
                    if (id < 0 || id > 53) continue; // Gioi han vung chest/vault

                    ItemStack stack = slot.getStack();
                    if (stack.isEmpty()) continue;

                    String itemId = Registries.ITEM.getId(stack.getItem()).toString();
                    info("- Slot " + slot.id + " (index " + id + "): " + itemId + " x" + stack.getCount());
                }
            }
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
                info("✅ Hoan tat.");
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

                // Tim item phu hop trong chest (slot index 0–53)
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

                warning("❌ Khong tim thay item " + itemId + " trong Vault.");
                layoutIndex++;
                waitTicks = 2;
                waiting = true;

            } catch (Exception e) {
                error("❌ Loi layout dong " + layoutIndex + ": " + e.getMessage());
                layoutIndex++;
            }
            return;
        }

        // Neu tay dang cam item, tiep tuc xu ly theo step
        switch (step) {
            case PICK_ONE -> {
                // Click phai de lay 1 item
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
    public void loadLayoutFromFile() {
        File file = new File("config/meteorist/sorted_inventory/default.json");
        if (!file.exists()) {
            warning("⚠ Khong tim thay file layout: default.json");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();
            List<String> entries = new ArrayList<>();

            for (JsonElement element : array) {
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                    entries.add(element.getAsString());
                }
            }

            layoutSetting.get().clear();
            layoutSetting.get().addAll(entries);

            info("📥 Da nap layout tu default.json voi " + entries.size() + " dong.");
        } catch (Exception e) {
            error("❌ Loi khi nap layout tu file: " + e.getMessage());
        }
    }
    public List<String> getLayoutSetting() {
        return layoutSetting.get();
    }

}
