package name.modid.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ElytraFlightMixin {
    
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        // Работает ТОЛЬКО когда элитры уже раскрыты
        if (player.isFallFlying()) {
            // Получаем направление, куда ты смотришь
            Vec3d look = player.getRotationVec(1.0F);
            
            // Настройка скорости (0.15 - стабильно, 0.4 - быстро)
            double speed = 0.25;

            // Движение вперед при нажатии W
            if (player.input.pressingForward) {
                // Применяем импульс к текущей скорости (addVelocity безопаснее)
                player.addVelocity(look.x * speed, look.y * speed, look.z * speed);
                
                // Ограничиваем максимальную скорость, чтобы не выкинуло с сервера
                Vec3d velocity = player.getVelocity();
                if (velocity.length() > 1.5) {
                    player.setVelocity(velocity.multiply(0.8));
                }
            } else {
                // Если кнопки не нажаты - просто зависаем (анти-падение)
                player.setVelocity(player.getVelocity().x, -0.001, player.getVelocity().z);
            }

            // Убираем урон от падения
            player.onLanding();
        }
    }
}




