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

        // 1. ЗАЩИТА ОТ КРАША (Для 1.20.1)
        // Проверяем, что игрок в мире и управление инициализировано
        if (player == null || player.clientWorld == null || player.input == null) return;

        // 2. АВТО-ВЗЛЕТ (Bypass активация)
        // Если падаем чуть-чуть — раскрываем элитры сами
        if (!player.isOnGround() && player.getVelocity().y < -0.1 && !player.isFallFlying()) {
            player.checkFallFlying();
        }

        // 3. ЛОГИКА ПОЛЕТА
        if (player.isFallFlying()) {
            Vec3d look = player.getRotationVec(1.0F);
            Vec3d v = player.getVelocity();

            // ПАРАМЕТРЫ ДЛЯ MYSTERYWORLD (Grim)
            double speed = 0.056; // Скорость (0.05 - 0.06 безопасно)
            double limit = 0.6;  // Порог, выше которого Grim кикает

            // Горизонтальное ускорение (только если не превышаем лимит)
            if (v.horizontalLength() < limit) {
                player.addVelocity(look.x * speed, 0, look.z * speed);
            }

            // ВЕРТИКАЛЬНЫЙ КОНТРОЛЬ (Анти-кик режим)
            if (player.input.jumping) {
                // Плавный набор высоты (на Пробел)
                player.addVelocity(0, 0.04, 0);
            } else if (player.input.sneaking) {
                // Быстрый спуск (на Shift)
                player.addVelocity(0, -0.2, 0);
            } else {
                // ГЛАЙД (Зависание): Grim считает падение -0.01 легитимным
                if (v.y < -0.01) {
                    player.setVelocity(v.x, -0.01, v.z);
                }
            }

            // ОБНУЛЕНИЕ УРОНА (Безопасно для 1.20.1)
            if (player.fallDistance > 1.0f) {
                player.fallDistance = 0;
            }
        }
    }
}










