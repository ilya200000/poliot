package name.modid;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.serializer.GsonConfigSerializer;

public class ModMenuImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        // Инициализация конфига при первом запуске
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        return parent -> AutoConfig.getConfigScreen(ModConfig.class, parent).get();
    }
}
