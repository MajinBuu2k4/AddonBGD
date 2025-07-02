package zgoly.meteorist.modules.WaypointFly;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.Vec3d;

import java.util.List;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;

public class CommandWaypoint extends Command {
    public CommandWaypoint() {
        super("wpf", "Lệnh điều khiển waypoint fly: add, delay, save, load, clear, list, reload.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        // .wpf add
        builder.then(literal("add")
                .executes(ctx -> {
                    Vec3d pos = mc.player.getPos();
                    String waypoint = String.format("%.1f %.1f %.1f", pos.x, pos.y, pos.z);
                    WaypointManager.getWaypoints().add(waypoint);
                    info("✅ Đã thêm waypoint: " + waypoint);
                    return 1;
                })
        );

        // .wpf delay <seconds>
        builder.then(literal("delay")
                .then(argument("seconds", integer(1))
                        .executes(ctx -> {
                            int seconds = ctx.getArgument("seconds", Integer.class);
                            WaypointManager.getWaypoints().add("delay " + seconds);
                            info("⏱️ Đã thêm delay: " + seconds + " giây.");
                            return 1;
                        })
                )
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
                    List<String> list = WaypointManager.getWaypoints();
                    if (list.isEmpty()) {
                        info("📭 Không có waypoint nào.");
                    } else {
                        info("📌 Có " + list.size() + " waypoint:");
                        for (int i = 0; i < list.size(); i++) {
                            ChatUtils.info("[" + (i + 1) + "] " + list.get(i));
                        }
                    }
                    return 1;
                })
        );

        // .wpf save
        builder.then(literal("save")
                .executes(ctx -> {
                    WaypointManager.save();
                    info("💾 Đã lưu vào file mặc định.");
                    return 1;
                })
        );

        // .wpf load
        builder.then(literal("load")
                .executes(ctx -> {
                    WaypointManager.load();
                    info("📂 Đã tải từ file mặc định.");
                    return 1;
                })
        );

        // .wpf reload (gọi WaypointFly.loadWaypoints())
        builder.then(literal("reload")
                .executes(ctx -> {
                    WaypointFly module = Modules.get().get(WaypointFly.class);
                    if (module != null) {
                        module.loadWaypoints();
                        info("📂 Đã reload WaypointFly.json thành công.");
                    } else {
                        error("❌ Không tìm thấy module WaypointFly.");
                    }
                    return 1;
                })
        );
    }
}
