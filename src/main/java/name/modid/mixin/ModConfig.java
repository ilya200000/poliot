package name.modid;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "poliot")
public class ModConfig implements ConfigData {
    public boolean totemEnabled = true;
    public int totemDelay = 50; // Задержка в мс (для Grim)

    public boolean autoBuyEnabled = false;
    public int shopSlot = 20; // Слот в магазине
    public String shopTitle = "Магазин";
}

