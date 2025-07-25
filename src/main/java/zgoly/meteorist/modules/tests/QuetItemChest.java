package zgoly.meteorist.modules.tests;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import zgoly.meteorist.Meteorist;

import java.util.HashSet;
import java.util.Set;

public class QuetItemChest extends Module {
    private final Set<Integer> scannedSlots = new HashSet<>();

    public QuetItemChest() {
        super(Meteorist.Custom, "quet-item-chest", "Quet item trong chest GUI va in ra log.");
    }

    @Override
    public void onActivate() {
        scannedSlots.clear();
    }

    @Override
    public void onDeactivate() {
        scannedSlots.clear();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!(mc.currentScreen instanceof HandledScreen<?> screen)) return;

        // Lấy tiêu đề GUI và kiểm tra
        String title = screen.getTitle().getString().toLowerCase();
        if (!(title.contains("chest") || title.contains("vault"))) return;

        ScreenHandler handler = screen.getScreenHandler();
        int chestSlotCount = handler.slots.size() - 36; // Trừ đi inventory của player

        for (int i = 0; i < chestSlotCount; i++) {
            if (scannedSlots.contains(i)) continue;

            Slot slot = handler.getSlot(i);
            ItemStack stack = slot.getStack();

            if (!stack.isEmpty()) {
                String itemName = stack.getItem().getName().getString();
                int count = stack.getCount();
                info("Slot " + i + ": " + itemName + " x" + count);
                scannedSlots.add(i);
            }
        }
    }
}
