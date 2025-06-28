package zgoly.meteorist.modules.movement;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import zgoly.meteorist.Meteorist;

import zgoly.meteorist.modules.movement.LicenseProtectedModule;

public class AutoTreo extends LicenseProtectedModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // Bat / Tat tung huong
    private final Setting<Boolean> enableForward = sgGeneral.add(new BoolSetting.Builder()
            .name("bat-di-tien")
            .description("Bat di chuyen tien")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> enableBackward = sgGeneral.add(new BoolSetting.Builder()
            .name("bat-di-lui")
            .description("Bat di chuyen lui")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> enableLeft = sgGeneral.add(new BoolSetting.Builder()
            .name("bat-di-trai")
            .description("Bat di chuyen trai")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> enableRight = sgGeneral.add(new BoolSetting.Builder()
            .name("bat-di-phai")
            .description("Bat di chuyen phai")
            .defaultValue(true)
            .build()
    );

    // Thoi gian tung huong
    private final Setting<Integer> forwardTime = sgGeneral.add(new IntSetting.Builder()
            .name("di-tien")
            .description("Thoi gian di tien (ms)")
            .defaultValue(1000)
            .min(0)
            .sliderMax(5000)
            .build()
    );

    private final Setting<Integer> backwardTime = sgGeneral.add(new IntSetting.Builder()
            .name("di-lui")
            .description("Thoi gian di lui (ms)")
            .defaultValue(1000)
            .min(0)
            .sliderMax(5000)
            .build()
    );

    private final Setting<Integer> leftTime = sgGeneral.add(new IntSetting.Builder()
            .name("di-trai")
            .description("Thoi gian di trai (ms)")
            .defaultValue(1000)
            .min(0)
            .sliderMax(5000)
            .build()
    );

    private final Setting<Integer> rightTime = sgGeneral.add(new IntSetting.Builder()
            .name("di-phai")
            .description("Thoi gian di phai (ms)")
            .defaultValue(1000)
            .min(0)
            .sliderMax(5000)
            .build()
    );

    // Lua chon khac
    private final Setting<Boolean> loop = sgGeneral.add(new BoolSetting.Builder()
            .name("lap-lai")
            .description("Lap lai vong di chuyen")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> holdShift = sgGeneral.add(new BoolSetting.Builder()
            .name("giu-shift")
            .description("Tu dong giu phim shift")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> autoJump = sgGeneral.add(new BoolSetting.Builder()
            .name("tu-nhay")
            .description("Tu dong nhay dinh ky")
            .defaultValue(false)
            .build()
    );

    private final Setting<Integer> jumpInterval = sgGeneral.add(new IntSetting.Builder()
            .name("thoi-gian-nhay")
            .description("Thoi gian giua cac lan nhay (ms)")
            .defaultValue(1000)
            .min(100)
            .sliderMax(10000)
            .build()
    );

    private int step = 0;
    private long stepStartTime = 0;
    private long lastJumpTime = 0;
    private boolean active = false;

    public AutoTreo() {
        super(Meteorist.Custom, "auto-treo", "Tu dong di cac huong tuy chinh");
    }

    @Override
    public void onActivate() {
        super.onActivate();
        if (!isActive()) return;


        step = 0;
        stepStartTime = System.currentTimeMillis();
        lastJumpTime = System.currentTimeMillis();
        active = true;
    }

    @Override
    public void onDeactivate() {
        resetKeys();
        mc.options.sneakKey.setPressed(false);
        active = false;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.world == null) return;
        long now = System.currentTimeMillis();

        if (!active) return;

        resetKeys();

        if (holdShift.get()) {
            mc.options.sneakKey.setPressed(true);
        }

        switch (step) {
            case 0 -> {
                if (enableForward.get()) {
                    mc.options.forwardKey.setPressed(true);
                    if (now - stepStartTime >= forwardTime.get()) {
                        stepStartTime = now;
                        step++;
                    }
                } else step++;
            }
            case 1 -> {
                if (enableBackward.get()) {
                    mc.options.backKey.setPressed(true);
                    if (now - stepStartTime >= backwardTime.get()) {
                        stepStartTime = now;
                        step++;
                    }
                } else step++;
            }
            case 2 -> {
                if (enableLeft.get()) {
                    mc.options.leftKey.setPressed(true);
                    if (now - stepStartTime >= leftTime.get()) {
                        stepStartTime = now;
                        step++;
                    }
                } else step++;
            }
            case 3 -> {
                if (enableRight.get()) {
                    mc.options.rightKey.setPressed(true);
                    if (now - stepStartTime >= rightTime.get()) {
                        stepStartTime = now;
                        step++;
                    }
                } else step++;
            }
            default -> {
                if (loop.get()) {
                    step = 0;
                    stepStartTime = now;
                } else {
                    toggle(); // tat module sau 1 vong
                }
            }
        }

        if (autoJump.get() && now - lastJumpTime >= jumpInterval.get()) {
            mc.player.jump();
            lastJumpTime = now;
        }
    }

    private void resetKeys() {
        mc.options.forwardKey.setPressed(false);
        mc.options.backKey.setPressed(false);
        mc.options.leftKey.setPressed(false);
        mc.options.rightKey.setPressed(false);
    }
}
