package zgoly.meteorist.settings;

import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.IVisible;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;

import java.util.function.Consumer;

public class Vec3dSetting extends Setting<Vec3d> {
    public Vec3dSetting(String name, String description, Vec3d defaultValue, Consumer<Vec3d> onChanged, Consumer<Setting<Vec3d>> onModuleActivated, IVisible visible) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
    }

    @Override
    protected Vec3d parseImpl(String str) {
        try {
            String[] split = str.trim().replace(",", " ").split("\\s+");
            return new Vec3d(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
        } catch (Exception e) {
            return null;
        }
    }


    @Override
    protected boolean isValueValid(Vec3d value) {
        return value != null;
    }

    @Override
    protected NbtCompound save(NbtCompound tag) {
        NbtCompound valueTag = new NbtCompound();
        valueTag.putDouble("x", get().x);
        valueTag.putDouble("y", get().y);
        valueTag.putDouble("z", get().z);
        tag.put("value", valueTag);
        return tag;
    }

    @Override
    protected Vec3d load(NbtCompound tag) {
        NbtCompound valueTag = tag.getCompound("value");
        return new Vec3d(
                valueTag.getDouble("x"),
                valueTag.getDouble("y"),
                valueTag.getDouble("z")
        );
    }

    public static class Builder extends SettingBuilder<Builder, Vec3d, Vec3dSetting> {
        public Builder() {
            super(new Vec3d(0, 0, 0)); // default mặc định là Vec3d(0, 0, 0)
        }

        @Override
        public Vec3dSetting build() {
            return new Vec3dSetting(name, description, defaultValue, onChanged, onModuleActivated, visible);
        }
    }
}
