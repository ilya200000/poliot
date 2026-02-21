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

        // Безопасная проверка: игрок должен быть в мире
        if (player != null && player.isFallFlying()) {
            
            // Используем MinecraftClient напрямую (самый стабильный способ в 1.21)
            MinecraftClient client = MinecraftClient.getInstance();
            boolean isForward = client.options.forwardKey.isPressed();
            boolean isJump = client.options.jumpKey.isPressed();

            Vec3d look = player.getRotationVec(1.0F);
            
            // СКОРОСТЬ 0.16 — золотая середина для Mystery/BedWars
            double speed = 0.16; 

            if (isForward) {
                // Толкаем игрока вперед
                player.addVelocity(look.x * speed, look.y * speed, look.z * speed);
            }

            // ANTI-GRAVITY: Не даем падать камнем вниз
            Vec3d v = player.getVelocity();
            if (v.y < -0.01) {
                // Плавное удержание высоты
                player.setVelocity(v.x, -0.01, v.z);
            }

            // Набор высоты на Пробел (имитация восходящего потока)
            if (isJump) {
                player.addVelocity(0, 0.08, 0);
            }

            // Сброс урона от падения
            player.onLanding();
        }
    }
}









