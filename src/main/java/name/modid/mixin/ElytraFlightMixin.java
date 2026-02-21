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

        // Проверяем, что игрок реально летит на элитрах
        if (player.isFallFlying()) {
            // Направление взгляда
            Vec3d look = player.getRotationVec(1.0F);
            
            // Скорость (0.25 - золотая середина)
            double speed = 0.25;

            // Если игрок нажимает клавиши движения (любые)
            // Мы проверяем это через стандартный метод, который не крашит
            if (player.input.movementForward > 0) {
                // Плавное ускорение вперед
                player.addVelocity(look.x * speed, look.y * speed, look.z * speed);
            } else {
                // Зависание в воздухе
                Vec3d velocity = player.getVelocity();
                player.setVelocity(velocity.x, -0.001, velocity.z);
            }

            // Убираем урон от падения
            player.fallDistance = 0;
        }
    }
}





