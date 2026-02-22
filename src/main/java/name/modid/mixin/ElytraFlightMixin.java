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
        
        // Проверка на мир и сессию (чтобы не вылетало при загрузке с Architectury)
        if (player == null || player.clientWorld == null || player.networkHandler == null) return;

        // 1. АВТО-ВЗЛЕТ (Если падаем, пытаемся раскрыть элитры)
        if (!player.isOnGround() && player.getVelocity().y < -0.2 && !player.isFallFlying()) {
            player.checkFallFlying();
        }

        if (player.isFallFlying()) {
            Vec3d look = player.getRotationVec(1.0F);
            Vec3d v = player.getVelocity();

            // НАСТРОЙКИ ДЛЯ MYSTERYWORLD (Grim Anticheat)
            double speed = 0.06; // Безопасное ускорение
            double limit = 0.55; // Максимальная горизонтальная скорость

            // 2. ГОРИЗОНТАЛЬНОЕ УСКОРЕНИЕ (Плавное "подталкивание")
            if (v.horizontalLength() < limit) {
                player.addVelocity(look.x * speed, 0, look.z * speed);
            }

            // 3. УПРАВЛЕНИЕ ВЕРТИКАЛЬЮ (Bypass: имитируем ванильное падение)
            if (player.input.jumping) {
                // Плавный подъем (на пробел)
                player.addVelocity(0, 0.05, 0);
            } else if (player.input.sneaking) {
                // Быстрый спуск (на Shift)
                player.addVelocity(0, -0.15, 0);
            } else {
                // ГЛИЙД-РЕЖИМ: фиксируем падение на грани фола (-0.01)
                // Это позволяет лететь прямо очень долго без флагов Grim
                if (v.y < -0.01) {
                    player.setVelocity(v.x, -0.01, v.z);
                }
            }

            // 4. ЗАЩИТА ОТ КРАША И УРОНА
            // Просто обнуляем дистанцию падения, чтобы Architectury не считал урон
            if (player.fallDistance > 1.0F) {
                player.fallDistance = 0;
            }
        }
    }
}












