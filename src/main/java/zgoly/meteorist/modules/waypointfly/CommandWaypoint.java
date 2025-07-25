package zgoly.meteorist.modules.WaypointFly;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.Vec3d;

import java.io.File;
import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;

public class CommandWaypoint extends Command {
    public CommandWaypoint() {
        super("wpf", "Lenh dieu khien waypoint fly: add, delay, flyon/off, command, save/load, clear, list, reload, goto, listprofiles.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        // .wpf add
        builder.then(literal("add")
                .executes(ctx -> {
                    Vec3d pos = mc.player.getPos();
                    String entry = String.format("POS %.2f %.2f %.2f", pos.x, pos.y, pos.z);
                    WaypointManager.add(entry);
                    info("✅ Da them waypoint: " + entry);
                    return 1;
                })
        );

        // .wpf goto
        builder.then(literal("goto")
                .executes(ctx -> {
                    Vec3d pos = mc.player.getPos();
                    String entry = String.format("GOTO %.2f %.2f %.2f", pos.x, pos.y, pos.z);
                    WaypointManager.add(entry);
                    info("📍 Da them waypoint GOTO: " + entry);
                    return 1;
                })
        );

        // .wpf delay <seconds>
        builder.then(literal("delay")
                .then(argument("seconds", integer(1))
                        .executes(ctx -> {
                            int seconds = ctx.getArgument("seconds", Integer.class);
                            String entry = "DELAY " + seconds;
                            WaypointManager.add(entry);
                            info("⏱️ Da them delay: " + seconds + " giay.");
                            return 1;
                        })
                )
        );

        // .wpf flyon
        builder.then(literal("flyon")
                .executes(ctx -> {
                    WaypointManager.add("FLYON");
                    info("🟢 Da them hanh dong: bat fly");
                    return 1;
                })
        );

        // .wpf flyoff
        builder.then(literal("flyoff")
                .executes(ctx -> {
                    WaypointManager.add("FLYOFF");
                    info("🔴 Da them hanh dong: tat fly");
                    return 1;
                })
        );

        // .wpf command <cmd>
        builder.then(literal("command")
                .then(argument("cmd", greedyString())
                        .executes(ctx -> {
                            String cmd = ctx.getArgument("cmd", String.class);
                            String entry = "COMMAND " + cmd;
                            WaypointManager.add(entry);
                            info("💬 Da them lenh: /" + cmd);
                            return 1;
                        })
                )
        );

        // .wpf clear
        builder.then(literal("clear")
                .executes(ctx -> {
                    WaypointManager.clearWaypoints();
                    info("🧹 Da xoa toan bo waypoint.");
                    return 1;
                })
        );

        // .wpf list
        builder.then(literal("list")
                .executes(ctx -> {
                    List<WaypointEntry> list = WaypointManager.getWaypoints();
                    if (list.isEmpty()) {
                        info("📭 Khong co waypoint nao.");
                    } else {
                        info("📌 Co " + list.size() + " waypoint:");
                        for (int i = 0; i < list.size(); i++) {
                            ChatUtils.info("[" + (i + 1) + "] " + list.get(i));
                        }
                    }
                    return 1;
                })
        );

        // .wpf save [name]
        builder.then(literal("save")
                .then(argument("name", greedyString())
                        .executes(ctx -> {
                            String name = ctx.getArgument("name", String.class);
                            WaypointManager.save(name);
                            info("💾 Da luu waypoint vao: " + name + ".json");
                            return 1;
                        })
                )
                .executes(ctx -> {
                    WaypointManager.save();
                    info("💾 Da luu vao file mac dinh.");
                    return 1;
                })
        );

        // .wpf load [name]
        builder.then(literal("load")
                .then(argument("name", greedyString())
                        .executes(ctx -> {
                            String name = ctx.getArgument("name", String.class);
                            WaypointManager.load(name);
                            info("📂 Da tai waypoint tu: " + name + ".json");
                            return 1;
                        })
                )
                .executes(ctx -> {
                    WaypointManager.load();
                    info("📂 Da tai tu file mac dinh.");
                    return 1;
                })
        );

        // .wpf reload [name]
        builder.then(literal("reload")
                .then(argument("name", greedyString())
                        .executes(ctx -> {
                            String name = ctx.getArgument("name", String.class);
                            WaypointManager.load(name); // Tai cau hinh cu the
                            WaypointFly module = Modules.get().get(WaypointFly.class);
                            if (module != null) {
                                // Cap nhat cai dat cua module WaypointFly voi cac waypoint da tai
                                module.getWaypoints().clear(); // Xoa waypoint cu trong module
                                module.getWaypoints().addAll(WaypointManager.getRawWaypoints()); // Them waypoint moi
                                info("🔁 Da reload cau hinh '" + name + "' vao WaypointFly.");
                            } else {
                                error("❌ Khong tim thay module WaypointFly.");
                            }
                            return 1;
                        })
                )
        );

        // .wpf listprofiles
        builder.then(literal("listprofiles")
                .executes(ctx -> {
                    File folder = new File("config/meteorist/waypoints");
                    File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));

                    if (files == null || files.length == 0) {
                        info("📭 Khong co profile nao.");
                    } else {
                        info("📂 Danh sach profiles:");
                        for (File file : files) {
                            String name = file.getName().replace(".json", "");
                            ChatUtils.info("• " + name);
                        }
                    }
                    return 1;
                })
        );

        // .wpf asdon
        builder.then(literal("asdon")
                .executes(ctx -> {
                    WaypointManager.add("ASDON");
                    info("🚨 Da bat che do phat hien ket (AntiStuck).");
                    return 1;
                })
        );

        // .wpf asdoff
        builder.then(literal("asdoff")
                .executes(ctx -> {
                    WaypointManager.add("ASDOFF");
                    info("✅ Da tat che do phat hien ket.");
                    return 1;
                })
        );
    }
}