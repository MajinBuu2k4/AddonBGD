package zgoly.meteorist.modules.WaypointFly;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.Vec3d;

public class CommandWaypoint extends Command {
    public CommandWaypoint() {
        super("wpf", "Lệnh điều khiển waypoint fly: add, save, load, clear.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        // .wpf add
        builder.then(literal("add")
                .executes(ctx -> {
                    Vec3d pos = mc.player.getPos();
                    WaypointManager.addWaypoint(pos);
                    info("✅ Đã thêm waypoint: " + formatVec(pos));
                    return 1;
                })
        );

        // .wpf clear
        builder.then(literal("clear")
                .executes(ctx -> {
                    WaypointManager.clearWaypoints();
                    info("🧹 Đã xoá toàn bộ waypoint.");
                    return 1;
                })
        );

        // .wpf list
        builder.then(literal("list")
                .executes(ctx -> {
                    var list = WaypointManager.getWaypoints();
                    if (list.isEmpty()) {
                        info("📭 Không có waypoint nào.");
                    } else {
                        info("📌 Có " + list.size() + " waypoint:");
                        for (int i = 0; i < list.size(); i++) {
                            Vec3d p = list.get(i);
                            ChatUtils.info("[" + (i + 1) + "] " + formatVec(p));
                        }
                    }
                    return 1;
                })
        );

        // .wpf save <name>
        builder.then(literal("save")
                .then(argument("name", StringArgumentType.word())
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "name");
                            boolean success = WaypointManager.saveTo(name);
                            if (success) info("💾 Đã lưu đường bay vào: " + name + ".json");
                            else error("❌ Lỗi khi lưu file.");
                            return 1;
                        })
                )
        );

        // .wpf load <name>
        builder.then(literal("load")
                .then(argument("name", StringArgumentType.word())
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "name");
                            boolean success = WaypointManager.loadFrom(name);
                            if (success) info("📂 Đã tải đường bay từ: " + name + ".json");
                            else error("❌ Không tìm thấy file: " + name + ".json");
                            return 1;
                        })
                )
        );
    }

    private String formatVec(Vec3d vec) {
        return String.format("[x=%.1f, y=%.1f, z=%.1f]", vec.x, vec.y, vec.z);
    }
}
