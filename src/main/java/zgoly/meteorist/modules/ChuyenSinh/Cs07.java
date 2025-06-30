package zgoly.meteorist.modules.ChuyenSinh;

import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.meteorclient.utils.player.ChatUtils;

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import zgoly.meteorist.Meteorist;
import zgoly.meteorist.modules.movement.LicenseProtectedModule;

public class Cs07 extends LicenseProtectedModule {
    private boolean hasClicked = false;
    private boolean inMineGui = false;

    public Cs07() {
        super(Meteorist.ChuyenSinh, "qua-cs07", "Qua CS07");
    }

    @Override
    public void onActivate() {
        super.onActivate();
        if (!isActive()) return;

        hasClicked = false;
        inMineGui = false;

        if (mc.player != null) {
            ChatUtils.sendPlayerMsg("/mine");
        }
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (event.screen instanceof GenericContainerScreen) {
            String title = event.screen.getTitle().getString();
            if (title.toLowerCase().contains("mine")) {
                inMineGui = true;
                hasClicked = false;
            }
        }
    }

    @EventHandler
    private void onRender(Render2DEvent event) {
        if (!inMineGui || !(mc.currentScreen instanceof GenericContainerScreen)) return;
        if (hasClicked || mc.player == null || mc.interactionManager == null) return;

        GenericContainerScreen screen = (GenericContainerScreen) mc.currentScreen;
        ScreenHandler handler = screen.getScreenHandler();

        int slotIndex = 19;

        if (slotIndex >= 0 && slotIndex < handler.slots.size()) {
            Slot slot = handler.getSlot(slotIndex);
            if (slot != null && slot.hasStack()) {
                mc.interactionManager.clickSlot(handler.syncId, slotIndex, 0, SlotActionType.PICKUP, mc.player);
                hasClicked = true;
                inMineGui = false;
                toggle();
            }
        }
    }
}
