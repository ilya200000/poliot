package name.modid.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class ElytraFlightMixin {
    
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        // Проверяем, что это игрок и он находится на клиенте
        if ((Object) this instanceof PlayerEntity player) {
            if (player.getWorld().isClient) {
                // Если игрок в режиме полета на элитрах
                if (player.isFallFlying()) {
                    // Активируем возможность полета
                    player.getAbilities().flying = true;
                    
                    // Если зажата кнопка прыжка - летим вверх
                    if (player.input != null && player.input.jumping) {
                        player.addVelocity(0, 0.05, 0);
                    }
                }
            }
        }
    }
}
