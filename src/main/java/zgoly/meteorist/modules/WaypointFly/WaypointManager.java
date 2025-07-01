package zgoly.meteorist.modules.WaypointFly;

import com.google.gson.*;
import net.minecraft.util.math.Vec3d;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class WaypointManager {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Path folderPath = Paths.get("config/meteorist/waypoints");
    private static final List<Vec3d> waypoints = new ArrayList<>();

    // ========== Public API ==========

    public static List<Vec3d> getWaypoints() {
        return waypoints;
    }

    public static void addWaypoint(Vec3d pos) {
        waypoints.add(pos);
    }

    public static void clearWaypoints() {
        waypoints.clear();
    }

    public static boolean saveTo(String name) {
        try {
            ensureFolder();
            Path file = folderPath.resolve(name + ".json");
            Files.write(file, gson.toJson(waypointsToList()).getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean loadFrom(String name) {
        try {
            ensureFolder();
            Path file = folderPath.resolve(name + ".json");
            if (!Files.exists(file)) return false;

            String json = Files.readString(file);
            JsonArray array = JsonParser.parseString(json).getAsJsonArray();

            waypoints.clear();
            for (JsonElement element : array) {
                JsonObject obj = element.getAsJsonObject();
                double x = obj.get("x").getAsDouble();
                double y = obj.get("y").getAsDouble();
                double z = obj.get("z").getAsDouble();
                waypoints.add(new Vec3d(x, y, z));
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ========== Helper ==========

    private static void ensureFolder() throws IOException {
        if (!Files.exists(folderPath)) Files.createDirectories(folderPath);
    }

    private static List<Map<String, Double>> waypointsToList() {
        List<Map<String, Double>> list = new ArrayList<>();
        for (Vec3d pos : waypoints) {
            Map<String, Double> map = new HashMap<>();
            map.put("x", pos.x);
            map.put("y", pos.y);
            map.put("z", pos.z);
            list.add(map);
        }
        return list;
    }
}
