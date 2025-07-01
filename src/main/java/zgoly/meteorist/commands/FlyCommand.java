package zgoly.meteorist.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandSource;
import zgoly.meteorist.modules.movement.FlySurvival;

public class FlyCommand extends Command {
    public FlyCommand() {
        super("fly", "Bật/tắt chế độ bay trong Survival.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(ctx -> {
            FlySurvival flyModule = Modules.get().get(FlySurvival.class);
            flyModule.toggle();
            ChatUtils.info("Fly " + (flyModule.isActive() ? "§aBẬT" : "§cTẮT"));
            return SINGLE_SUCCESS;
        });

        builder.then(literal("on").executes(ctx -> {
            FlySurvival flyModule = Modules.get().get(FlySurvival.class);
            if (!flyModule.isActive()) flyModule.toggle();
            ChatUtils.info("Fly §aBẬT");
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("off").executes(ctx -> {
            FlySurvival flyModule = Modules.get().get(FlySurvival.class);
            if (flyModule.isActive()) flyModule.toggle();
            ChatUtils.info("Fly §cTẮT");
            return SINGLE_SUCCESS;
        }));
    }
}
