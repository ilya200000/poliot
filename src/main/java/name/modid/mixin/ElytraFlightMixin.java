package name.modid.mixin;

import net.minecraft.client.MinecraftClient;
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

        // Проверяем полет на элитрах
        if (player.isFallFlying()) {
            // Получаем доступ к кнопкам напрямую через движок, а не через игрока
            boolean isForwardPressed = MinecraftClient.getInstance().options.forwardKey.isPressed();
            boolean isJumpPressed = MinecraftClient.getInstance().options.jumpKey.isPressed();

            Vec3d look = player.getRotationVec(1.0F);
            double speed = 0.3; // Твоя скорость полета

            if (isForwardPressed) {
                // Двигаем игрока вперед
                player.addVelocity(look.x * speed, look.y * speed, look.z * speed);
            } else {
                // Стабильное планирование (зависание)
                Vec3d v = player.getVelocity();
                player.setVelocity(v.x, -0.005, v.z);
            }

            // Если жмешь прыжок - летишь выше
            if (isJumpPressed) {
                player.addVelocity(0, 0.1, 0);
            }

            // Сброс урона от падения
            player.fallDistance = 0;
        }
    }
}





