package zgoly.meteorist.modules.WaypointFly;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.minecraft.util.math.Vec3d;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class WaypointManager {
    private static final List<String> waypoints = new ArrayList<>();
    private static final File FILE = new File("config/meteorist/waypoints/WaypointFly.json");
    private static final Gson GSON = new Gson();

    public static void addWaypoint(Vec3d vec) {
        // Lưu theo định dạng: "x y z"
        String wp = String.format("%.1f %.1f %.1f", vec.x, vec.y, vec.z);
        waypoints.add(wp);
    }

    public static void clearWaypoints() {
        waypoints.clear();
    }

    public static List<String> getWaypoints() {
        return waypoints;
    }

    public static void save() {
        try {
            FILE.getParentFile().mkdirs();
            try (Writer writer = new FileWriter(FILE)) {
                GSON.toJson(waypoints, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        try {
            if (!FILE.exists()) return;

            try (Reader reader = new FileReader(FILE)) {
                Type listType = new TypeToken<List<String>>() {}.getType();
                List<String> loaded = GSON.fromJson(reader, listType);
                waypoints.clear();
                if (loaded != null) waypoints.addAll(loaded);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
