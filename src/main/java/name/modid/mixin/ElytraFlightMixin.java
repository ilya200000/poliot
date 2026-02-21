package name.modid.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class ElytraFlightMixin {
    
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        // 1. Проверяем, что мы на клиенте и игрок существует в мире
        if (player.getWorld() != null && player.getWorld().isClient) {
            
            // 2. Работает только при активном полете на элитрах
            if (player.isFallFlying()) {
                
                // Направление взгляда
                Vec3d look = player.getRotationVec(1.0F);
                double speed = 0.5; // Скорость (0.5 - быстро, 0.2 - безопасно для серверов)

                // 3. ЛЕТИМ вперед (только если нажата кнопка движения W)
                // Используем проверку горизонтальной скорости для стабильности
                if (player.forwardSpeed > 0) {
                    player.setVelocity(look.x * speed, look.y * speed, look.z * speed);
                } else {
                    // Простое зависание/планирование без падения вниз
                    Vec3d currentVel = player.getVelocity();
                    player.setVelocity(currentVel.x, -0.005, currentVel.z);
                }
                
                // Убираем урон от падения
                player.onLanding();
            }
        }
    }
}




