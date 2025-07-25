package zgoly.meteorist.modules.WaypointFly;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class WaypointManager {
    private static final String FOLDER_PATH = "config/meteorist/waypoints";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<String>>() {}.getType();

    private static final List<String> rawWaypoints = new ArrayList<>();

    public static void add(String entry) {
        rawWaypoints.add(entry.trim());
    }

    public static void clearWaypoints() {
        rawWaypoints.clear();
    }

    public static List<String> getRawWaypoints() {
        return rawWaypoints;
    }

    public static List<WaypointEntry> getWaypoints() {
        List<WaypointEntry> parsed = new ArrayList<>();
        for (String line : rawWaypoints) {
            try {
                parsed.add(new WaypointEntry(line));
            } catch (Exception e) {
                System.err.println("❌ Loi khi phan tich waypoint: " + line);
                e.printStackTrace();
            }
        }
        return parsed;
    }

    // Luu file mac dinh
    public static void save() {
        save("default");
    }

    // Tai file mac dinh
    public static void load() {
        load("default");
    }

    // Luu profile theo ten
    public static void save(String name) {
        File file = new File(FOLDER_PATH, name + ".json");
        file.getParentFile().mkdirs();

        try (Writer writer = new FileWriter(file)) {
            GSON.toJson(rawWaypoints, LIST_TYPE, writer);
        } catch (IOException e) {
            System.err.println("❌ Khong the luu waypoint vao file: " + file.getPath());
            e.printStackTrace();
        }
    }

    // Tai profile theo ten
    public static void load(String name) {
        File file = new File(FOLDER_PATH, name + ".json");
        rawWaypoints.clear();

        if (!file.exists()) {
            System.err.println("⚠️ Khong tim thay file waypoint: " + file.getPath());
            return;
        }

        try (Reader reader = new FileReader(file)) {
            List<String> loaded = GSON.fromJson(reader, LIST_TYPE);
            if (loaded != null) rawWaypoints.addAll(loaded);
        } catch (IOException e) {
            System.err.println("❌ Khong the doc file waypoint: " + file.getPath());
            e.printStackTrace();
        }
    }
}
