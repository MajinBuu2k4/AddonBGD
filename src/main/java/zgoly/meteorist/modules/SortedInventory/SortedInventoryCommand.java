package zgoly.meteorist.modules.SortedInventory;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;

public class SortedInventoryCommand extends Command {
    public SortedInventoryCommand() {
        super("sinv", "Reload or manage sorted inventory layouts.");
    }

    public void run(String[] args) {
        if (args.length == 0) {
            ChatUtils.info("Usage: .sinv reload");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                LayoutManager.reload();
                ChatUtils.info("Reloaded SortedInventory layouts.");
            }

            default -> ChatUtils.error("Unknown subcommand: " + args[0]);
        }
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {

    }
}

