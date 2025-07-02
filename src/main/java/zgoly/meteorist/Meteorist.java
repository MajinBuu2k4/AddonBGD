package zgoly.meteorist;

import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zgoly.meteorist.commands.*;
import zgoly.meteorist.hud.TextPresets;
import zgoly.meteorist.modules.*;
import zgoly.meteorist.modules.FlyGotoMine.FlyGotoMine;
import zgoly.meteorist.modules.WaypointFly.WaypointFly;
import zgoly.meteorist.modules.WaypointFly.CommandWaypoint;
import zgoly.meteorist.modules.autocrafter.AutoCrafter;
import zgoly.meteorist.modules.autologin.AutoLogin;
import zgoly.meteorist.modules.autotrade.AutoTrade;
import zgoly.meteorist.modules.instructions.Instructions;
import zgoly.meteorist.modules.movement.*;
import zgoly.meteorist.modules.placer.Placer;
import zgoly.meteorist.modules.rangeactions.RangeActions;
import zgoly.meteorist.modules.slotclick.SlotClick;
import zgoly.meteorist.settings.StringPairSetting;
import zgoly.meteorist.utils.misc.MeteoristStarscript;
import zgoly.meteorist.modules.ChuyenSinh.*;
import zgoly.meteorist.commands.FlyCommand;

//Tự thêm


import java.util.Random;

public class Meteorist extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger("Meteorist");
    public static final Category CATEGORY = new Category("Meteorist", Items.FIRE_CHARGE.getDefaultStack());
    public static final Category Custom = new Category("Custom");
    public static final Category ChuyenSinh = new Category("ChuyenSinh");
    public static final HudGroup HUD_GROUP = new HudGroup("Meteorist");
    private static final String[] MESSAGES = {
            "Meteorist is coming",
            "Meteorist enabled",
            "Meteorist is here",
            "Meteorist will save us",
            "Meteorist joined the game",
            "Meteorist is ready to go",
            "Meteorist is on the move",
            "Meteorist... Was never real?"
    };
    public static String MOD_ID = "meteorist";
    public static GuiTexture ARROW_UP;
    public static GuiTexture ARROW_DOWN;
    public static GuiTexture COPY;
    public static GuiTexture EYE;

    public static Identifier identifier(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        // Log random message
        Random random = new Random();
        LOG.info(MESSAGES[random.nextInt(MESSAGES.length)]);

        // Factories
        LOG.info("Registering custom factories...");
        StringPairSetting.register();

        // Modules
        LOG.info("Registering modules...");
        Modules.get().add(new AutoCrafter());
        Modules.get().add(new AutoFeed());
        Modules.get().add(new AutoFix());
        Modules.get().add(new AutoHeal());
        Modules.get().add(new AutoInteract());
        Modules.get().add(new AutoLeave());
        Modules.get().add(new AutoLogin());
        Modules.get().add(new AutoMud());
        Modules.get().add(new AutoSleep());
        Modules.get().add(new AutoSneak());
        Modules.get().add(new AutoTrade());
        Modules.get().add(new BoatControl());
        Modules.get().add(new DisconnectSound());
        Modules.get().add(new DmSpam());
        Modules.get().add(new DoubleDoorsInteract());
        Modules.get().add(new EntityInteract());
        Modules.get().add(new Grid());
        Modules.get().add(new Instructions());
        Modules.get().add(new ItemSucker());
        Modules.get().add(new JumpFlight());
        Modules.get().add(new JumpJump());
        Modules.get().add(new Placer());
        Modules.get().add(new SlotClick());
        Modules.get().add(new NerdVision());
        Modules.get().add(new RangeActions());
        Modules.get().add(new ZAimbot());
        Modules.get().add(new ZKillaura());
        Modules.get().add(new ZoomPlus());



        //Tự thêm
        Modules.get().add(new SaveTP());
        Modules.get().add(new AutoTreo());
        Modules.get().add(new AutoLoginCum());
        Modules.get().add(new AutoClickLoginCum());
        Modules.get().add(new LookAtOnce());
        Modules.get().add(new AutoTraLoi());
        Modules.get().add(new AutoQuaMine());
        Modules.get().add(new AutoQuaAFK());
        Modules.get().add(new GotoBaritone());
        Modules.get().add(new ThongBaoBoss());
        Modules.get().add(new AutoGotoMine());
        Modules.get().add(new AutoChao());
        Modules.get().add(new FlyGotoMine());
        Modules.get().add(new FlySurvival());
        Modules.get().add(new WaypointFly());



        //ChuyenSinh
        Modules.get().add(new Cs00());
        Modules.get().add(new Cs01());
        Modules.get().add(new Cs02());
        Modules.get().add(new Cs03());
        Modules.get().add(new Cs04());
        Modules.get().add(new Cs05());
        Modules.get().add(new Cs06());
        Modules.get().add(new Cs07());
        Modules.get().add(new Cs08());
        Modules.get().add(new Cs09());
        Modules.get().add(new Cs10());




        // Commands
        LOG.info("Registering commands...");
        Commands.add(new DataCommand());
        Commands.add(new InstructionsCommand());
        Commands.add(new InteractCommand());
        Commands.add(new PlayersInfoCommand());
        Commands.add(new ActiveCommand());
        Commands.add(new FlyCommand());
        Commands.add(new CommandWaypoint());






        // HUD text presets
        LOG.info("Registering HUD text presets...");
        MeteoristStarscript.init();
        Hud.get().register(TextPresets.INFO);

        // Icons
        ARROW_UP = GuiRenderer.addTexture(identifier("textures/icons/gui/arrow_up.png"));
        ARROW_DOWN = GuiRenderer.addTexture(identifier("textures/icons/gui/arrow_down.png"));
        COPY = GuiRenderer.addTexture(identifier("textures/icons/gui/copy.png"));
        EYE = GuiRenderer.addTexture(identifier("textures/icons/gui/eye.png"));
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
        Modules.registerCategory(Custom);
        Modules.registerCategory(ChuyenSinh);
    }

    @Override
    public String getPackage() {
        return getRepo().owner() + "." + getRepo().name();
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("zgoly", "meteorist");
    }
}