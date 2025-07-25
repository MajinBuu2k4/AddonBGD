package zgoly.meteorist.modules.WaypointFly;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.gui.widgets.*;
import meteordevelopment.meteorclient.gui.screens.settings.SettingsScreen;
import meteordevelopment.meteorclient.gui.themes.Theme;
import meteordevelopment.meteorclient.systems.modules.Module;

import net.minecraft.client.gui.screen.Screen;

import java.io.File;

public class WaypointFlyGUI extends SettingsScreen {
    private final WaypointFly module;

    public WaypointFlyGUI(Module module) {
        super(module);
        this.module = (WaypointFly) module;
    }

    @Override
    public void initWidgets() {
        super.initWidgets();

        Theme theme = theme();
        WTable table = add(theme.table()).expandX().top().widget();

        // Tiêu đề section
        table.add(theme.label("Waypoints Manager")).expandX().widget();
        table.row();

        // Input + Add
        WTextBox inputBox = table.add(theme.textBox(""))
                .expandX()
                .widget();

        table.add(theme.button("Add")).expandX().widget().action(() -> {
            String wp = inputBox.get();
            if (!wp.isEmpty()) {
                module.addWaypoint(wp);
                inputBox.set("");
            }
        });
        table.row();

        // Remove All
        table.add(theme.button("Remove All")).expandX().widget().action(() -> {
            module.clearWaypoints();
        });
        table.row();

        // Hiện danh sách waypoints
        for (String wp : module.getWaypoints()) {
            table.add(theme.label("• " + wp)).expandX().widget();
            table.row();
        }

        // Save / Load buttons
        table.add(theme.button("Save Waypoints")).expandX().widget().action(() -> {
            module.saveWaypoints();
        });
        table.row();

        table.add(theme.button("Load Waypoints")).expandX().widget().action(() -> {
            module.loadWaypoints();
        });
        table.row();

        // Open folder
        table.add(theme.button("Open Folder")).expandX().widget().action(() -> {
            try {
                File folder = new File("config/meteorist/waypoints/");
                java.awt.Desktop.getDesktop().open(folder);
            } catch (Exception e) {
                module.warning("❌ Không thể mở thư mục.");
            }
        });
    }
}
