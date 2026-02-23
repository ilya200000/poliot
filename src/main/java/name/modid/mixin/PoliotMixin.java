package name.modid.mixin;

import name.modid.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class PoliotMixin {
    private int totemTimer = 0;

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        if (config.totemEnabled && player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
            if (totemTimer <= 0) {
                for (int i = 9; i <= 44; i++) {
                    if (player.getInventory().getStack(i == 44 ? 40 : i).getItem() == Items.TOTEM_OF_UNDYING) {
                        int slot = i == 44 ? 45 : i;
                        MinecraftClient.getInstance().interactionManager.clickSlot(player.currentScreenHandler.syncId, slot, 40, SlotActionType.SWAP, player);
                        totemTimer = config.totemDelay / 50; 
                        break;
                    }
                }
            }
        }
        if (totemTimer > 0) totemTimer--;
    }
}

@Mixin(GenericContainerScreen.class)
abstract class ShopMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void onShopRender(CallbackInfo ci) {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        if (!config.shopEnabled) return;

        GenericContainerScreen screen = (GenericContainerScreen) (Object) this;
        if (screen.getTitle().getString().contains(config.shopTitle)) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.interactionManager != null && client.player != null) {
                client.interactionManager.clickSlot(screen.getScreenHandler().syncId, config.shopSlot, 0, SlotActionType.QUICK_MOVE, client.player);
            }
        }
    }
}
