package name.modid;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "poliot")
public class ModConfig implements ConfigData {
    public boolean totemEnabled = true;
    public int totemDelay = 60; 

    public boolean shopEnabled = false;
    public int shopSlot = 20;
    public String shopTitle = "Магазин";
}
