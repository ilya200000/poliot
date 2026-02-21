package name.modid.mixin;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class ElytraFlightMixin {
    
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        
        // Работает только на клиенте, чтобы не было десинхрона
        if (player.getWorld().isClient) {
            if (player.isFallFlying()) {
                // Разрешаем полет
                player.getAbilities().flying = true;
                
                // Устанавливаем стабильную скорость
                player.getAbilities().setFlySpeed(0.05f);
            }
        }
    }
}


