package name.modid;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public class ModMenuImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        try {
            AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        } catch (Exception ignored) {}
        return parent -> AutoConfig.getConfigScreen(ModConfig.class, parent).get();
    }
}
