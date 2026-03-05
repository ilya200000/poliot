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
        // Кастуем текущий объект в игрока
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        // Проверяем, активированы ли элитры (режим планирования)
        if (player.isFallFlying()) {
            // МИНИМАЛЬНАЯ СКОРОСТЬ (0.12) — чтобы Grim не дергал камеру
            double speed = 0.12; 
            
            // В 1.21.1 направление взгляда берется через getRotationVector
            Vec3d look = player.getRotationVector();
            
            // Если зажата клавиша вперед (W)
            if (player.input.pressingForward) {
                // Применяем вектор скорости по направлению прицела
                player.setVelocity(
                    look.x * speed, 
                    look.y * speed, 
                    look.z * speed
                );
            } else {
                // Имитируем естественное падение, чтобы сервер не заподозрил зависание
                player.setVelocity(0, -0.01, 0);
            }
        }
    }
}
