package zgoly.meteorist.modules.WaypointFly;

import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.screens.ModuleScreen;
import meteordevelopment.meteorclient.gui.widgets.WLabel;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;

import java.util.ArrayList;
import java.util.List;

public class WaypointFlyScreen extends ModuleScreen {
    private final WaypointFly module;

    private WVerticalList waypointList;
    private WTextBox inputBox;

    public WaypointFlyScreen(WaypointFly module) {
        super(GuiThemes.get(), module);
        this.module = module;
    }

    @Override
    public void initWidgets() {
        // Layout chính
        WVerticalList root = add(theme.verticalList()).expandX().widget();

        // Tiêu đề
        root.add(theme.label("📍 Waypoint Fly Manager"));

        // Danh sách waypoint
        waypointList = theme.verticalList();
        root.add(waypointList);  // Không dùng chain
        updateWaypointList();

        // Ô nhập text
        inputBox = root.add(theme.textBox("")).expandX().widget();

        // Nhóm nút chức năng
        WHorizontalList buttons = root.add(theme.horizontalList()).expandX().widget();

        // ➕ Add
        WButton add = buttons.add(theme.button("➕ Add")).widget();
        add.action = () -> {
            String wp = inputBox.get();
            if (!wp.isBlank()) {
                module.addWaypoint(wp);
                inputBox.set("");
                updateWaypointList();
            }
        };

        // ➖ Remove Last
        WButton remove = buttons.add(theme.button("➖ Remove Last")).widget();
        remove.action = () -> {
            List<String> list = new ArrayList<>(module.getWaypoints());
            if (!list.isEmpty()) {
                list.remove(list.size() - 1);
                module.clearWaypoints();
                list.forEach(module::addWaypoint);
                updateWaypointList();
            }
        };

        // 💾 Save
        WButton save = buttons.add(theme.button("💾 Save")).widget();
        save.action = module::saveWaypoints;

        // 📂 Load
        WButton load = buttons.add(theme.button("📂 Load")).widget();
        load.action = () -> {
            module.loadWaypoints();
            updateWaypointList();
        };

        // 🧹 Clear
        WButton clear = buttons.add(theme.button("🧹 Clear")).widget();
        clear.action = () -> {
            module.clearWaypoints();
            updateWaypointList();
        };
    }

    private void updateWaypointList() {
        waypointList.clear();
        List<String> waypoints = module.getWaypoints();
        if (waypoints.isEmpty()) {
            waypointList.add(theme.label("📭 Không có waypoint nào."));
        } else {
            for (int i = 0; i < waypoints.size(); i++) {
                String text = "[" + (i + 1) + "] " + waypoints.get(i);
                waypointList.add(theme.label(text));
            }
        }
    }
}
