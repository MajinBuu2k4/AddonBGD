package zgoly.meteorist.modules.movement;

import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

public abstract class LicenseProtectedModule extends Module {
    // ✅ Đây là đường dẫn TẬP TIN .lic, không phải thư mục
    private static final String LIC_PATH = System.getProperty("user.home") + "/AppData/Roaming/Microsoft/Windows/LicenseSys/mssys.lic";

    private static final String EXPECTED_MODULE = "546F6F6C446144756F63416374697665"; // ToolDaDuocActive

    protected LicenseProtectedModule(Category category, String name, String description) {
        super(category, name, description);
    }

    protected boolean checkLicense() {
        try {
            File licFile = new File(LIC_PATH);
            if (!licFile.exists()) return false;

            try (FileReader reader = new FileReader(licFile)) {
                Gson gson = new Gson();
                Map<String, String> data = gson.fromJson(reader, new TypeToken<Map<String, String>>() {}.getType());
                return EXPECTED_MODULE.equals(data.get("module"));
            }
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void onActivate() {
        if (!checkLicense()) {
            info("🔒 Module chưa được kích hoạt hợp lệ. Hãy liên hệ MajinBuu2k4 để mua key.");
            toggle();
        }
    }
}
