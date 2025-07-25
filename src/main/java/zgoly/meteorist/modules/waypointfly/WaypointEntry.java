package zgoly.meteorist.modules.WaypointFly;

import net.minecraft.util.math.Vec3d;

public class WaypointEntry {
    public enum Type {
        POS,
        DELAY,
        FLYON,
        FLYOFF,
        COMMAND,
        GOTO,
        ASDON,
        ASDOFF
    }


    public final Type type;
    public double x, y, z;      // Dung cho POS & GOTO
    public int delay;           // Dung cho DELAY
    public String command = ""; // Dung cho COMMAND

    public WaypointEntry(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) throw new IllegalArgumentException("Empty waypoint line");

        String[] split = trimmed.split("\\s+", 2);
        String head = split[0].toUpperCase();

        switch (head) {
            case "POS" -> {
                if (split.length < 2) throw new IllegalArgumentException("Thieu toa do POS");
                String[] coords = split[1].split("\\s+");
                if (coords.length < 3) throw new IllegalArgumentException("Can 3 toa do cho POS");
                x = Double.parseDouble(coords[0]);
                y = Double.parseDouble(coords[1]);
                z = Double.parseDouble(coords[2]);
                type = Type.POS;
            }

            case "GOTO" -> {
                if (split.length < 2) throw new IllegalArgumentException("Thieu toa do GOTO");
                String[] coords = split[1].split("\\s+");
                if (coords.length < 3) throw new IllegalArgumentException("Can 3 toa do cho GOTO");
                x = Double.parseDouble(coords[0]);
                y = Double.parseDouble(coords[1]);
                z = Double.parseDouble(coords[2]);
                type = Type.GOTO;
            }

            case "DELAY" -> {
                delay = 3; // Mac dinh 3 giay neu khong co so
                if (split.length >= 2) {
                    try {
                        delay = Integer.parseInt(split[1]);
                    } catch (NumberFormatException ignored) {}
                }
                type = Type.DELAY;
            }

            case "FLYON" -> type = Type.FLYON;
            case "FLYOFF" -> type = Type.FLYOFF;

            case "COMMAND" -> {
                command = (split.length >= 2) ? split[1] : "";
                type = Type.COMMAND;
            }

            case "ASDON" -> type = Type.ASDON;
            case "ASDOFF" -> type = Type.ASDOFF;

            default -> throw new IllegalArgumentException("Khong hieu loai waypoint: " + head);
        }
    }


    public Vec3d toVec3d() {
        return (type == Type.POS || type == Type.GOTO) ? new Vec3d(x, y, z) : null;
    }

    @Override
    public String toString() {
        return switch (type) {
            case POS -> String.format("POS %.1f %.1f %.1f", x, y, z);
            case GOTO -> String.format("GOTO %.1f %.1f %.1f", x, y, z);
            case DELAY -> "DELAY " + delay;
            case FLYON -> "FLYON";
            case FLYOFF -> "FLYOFF";
            case COMMAND -> "COMMAND " + command;
            case ASDON -> "ASDON";
            case ASDOFF -> "ASDOFF";
        };
    }

}
