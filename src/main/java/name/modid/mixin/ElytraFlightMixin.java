package name.modid.mixin;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class ElytraFlightMixin {
    
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        
        // Проверяем, надеты ли элитры и летит ли игрок
        if (player.isFallFlying()) {
            // Включаем возможность летать как в креативе
            player.getAbilities().flying = true;
            
            // Устанавливаем небольшую скорость, чтобы не кикало
            if (player.getAbilities().getFlySpeed() < 0.05f) {
                player.getAbilities().setFlySpeed(0.05f);
            }
        }
    }
}


