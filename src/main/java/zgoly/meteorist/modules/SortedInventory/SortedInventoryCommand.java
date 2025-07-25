package zgoly.meteorist.modules.SortedInventory;

import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

import com.google.gson.*;

import java.io.*;
import java.util.*;


public class SortedInventoryCommand extends Command {
    public SortedInventoryCommand() {
        super("sinv", "Lenh dieu khien SortedInventory.");
    }

    private int convertToChestInventorySlot(int rawSlot) {
        if (rawSlot >= 0 && rawSlot <= 8) return 81 + rawSlot;         // Hotbar
        if (rawSlot >= 9 && rawSlot <= 35) return 54 + (rawSlot - 9);  // Main inventory
        return rawSlot;
    }

    @Override
    public void build(com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.command.CommandSource> builder) {
        builder.then(literal("save")
                .executes(ctx -> {
                    List<String> layout = new ArrayList<>();

                    for (int slot = 0; slot < 36; slot++) {
                        ItemStack stack = mc.player.getInventory().getStack(slot);
                        if (!stack.isEmpty()) {
                            String itemId = Registries.ITEM.getId(stack.getItem()).toString();
                            int mappedSlot = convertToChestInventorySlot(slot);
                            layout.add(mappedSlot + "|" + itemId + "|" + stack.getCount());
                        }
                    }

                    File folder = new File("config/meteorist/sorted_inventory");
                    if (!folder.exists()) folder.mkdirs();

                    File file = new File(folder, "default.json");

                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write("[\n");
                        for (int i = 0; i < layout.size(); i++) {
                            writer.write("  \"" + layout.get(i) + "\"");
                            if (i < layout.size() - 1) writer.write(",");
                            writer.write("\n");
                        }
                        writer.write("]");
                        info("💾 Da luu layout vao: default.json");
                    } catch (IOException e) {
                        error("❌ Loi khi luu layout: " + e.getMessage());
                    }

                    return 1;
                })
        );

        builder.then(literal("reload")
                .executes(ctx -> {
                    SortedInventory mod = null;
                    for (Module module : Modules.get().getAll()) {
                        if (module instanceof SortedInventory) {
                            mod = (SortedInventory) module;
                            break;
                        }
                    }

                    if (mod == null) {
                        error("❌ Khong tim thay module SortedInventory.");
                        return 0;
                    }

                    File file = new File("config/meteorist/sorted_inventory/default.json");
                    if (!file.exists()) {
                        error("⚠ Khong tim thay file: default.json");
                        return 0;
                    }

                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        JsonArray array = JsonParser.parseReader(reader).getAsJsonArray();
                        List<String> entries = new ArrayList<>();

                        for (JsonElement element : array) {
                            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                                entries.add(element.getAsString());
                            }
                        }

                        mod.getLayoutSetting().clear();
                        mod.getLayoutSetting().addAll(entries);

                        info("📥 Da nap layout tu default.json voi " + entries.size() + " dong.");
                    } catch (Exception e) {
                        error("❌ Loi khi nap layout: " + e.getMessage());
                    }

                    return 1;
                })
        );
    }
}
