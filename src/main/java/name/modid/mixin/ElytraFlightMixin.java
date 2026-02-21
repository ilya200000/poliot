package name.modid.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class ElytraFlightMixin {
    
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        // Проверяем, является ли сущность игроком
        if ((Object) this instanceof PlayerEntity player) {
            // Если игрок летит на элитрах (или просто упал и нажал прыжок)
            if (player.isFallFlying()) {
                // Включаем режим полета
                player.getAbilities().flying = true;
                
                // Устанавливаем комфортную скорость полета
                player.getAbilities().setFlySpeed(0.05f);
            }
        }
    }
}

