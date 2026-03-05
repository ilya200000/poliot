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
        // Получаем объект игрока
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        // Проверяем, раскрыты ли элитры
        if (player.isFallFlying()) {
            // УМЕНЬШЕННАЯ СКОРОСТЬ (была ~0.7, теперь 0.15)
            double speed = 0.15;
            
            // Получаем вектор направления взгляда
            Vec3d look = player.getRotationVector();
            
            // Если зажата кнопка движения вперед (W)
            if (player.input.pressingForward) {
                // Умножаем направление на скорость
                double vX = look.x * speed;
                double vY = look.y * speed;
                double vZ = look.z * speed;
                
                // Устанавливаем новую скорость игроку (метод setVelocity)
                player.setVelocity(vX, vY, vZ);
            } else {
                // Если W не нажата — медленное планирование вниз (чтобы не кикнул Grim)
                player.setVelocity(0, -0.01, 0);
            }
        }
    }
}
