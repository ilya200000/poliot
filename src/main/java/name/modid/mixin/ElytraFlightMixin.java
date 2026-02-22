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

        // Базовая защита от вылетов на 1.20.1
        if (player == null || player.clientWorld == null || player.input == null) return;

        // 1. АВТО-ВЗЛЕТ (Если падаем - раскрываем элитры)
        if (!player.isOnGround() && player.getVelocity().y < -0.1 && !player.isFallFlying()) {
            player.checkFallFlying();
        }

        if (player.isFallFlying()) {
            Vec3d look = player.getRotationVec(1.0F);
            Vec3d v = player.getVelocity();

            // --- НАСТРОЙКИ BOOST (Для MysteryWorld / Grim) ---
            // 0.08 - очень быстро (может кикать), 0.05 - стабильно
            double boostPower = 0.058; 
            double maxSpeed = 0.7; // Лимит скорости, чтобы не забанило

            // 2. ЛОГИКА УСКОРЕНИЯ (Boost)
            // Ускоряемся, если зажата клавиша "Вперед" (W)
            if (player.input.pressingForward) {
                if (v.horizontalLength() < maxSpeed) {
                    // Толкаем игрока по вектору взгляда (как фейерверк)
                    player.addVelocity(
                        look.x * boostPower,
                        look.y * boostPower * 0.5, // Небольшой буст вверх
                        look.z * boostPower
                    );
                }
            }

            // 3. ВЕРТИКАЛЬНЫЙ КОНТРОЛЬ (Bypass для Grim)
            if (player.input.jumping) {
                // Взлет на Пробел
                player.addVelocity(0, 0.05, 0);
            } else if (player.input.sneaking) {
                // Быстрый спуск на Shift
                player.addVelocity(0, -0.2, 0);
            } else {
                // ГЛАЙД: Фиксируем падение на -0.01 (идеально для обхода Fly)
                if (v.y < -0.01) {
                    player.setVelocity(v.x, -0.01, v.z);
                }
            }

            // 4. ЗАЩИТА
            player.fallDistance = 0; // Нет урона при посадке
        }
    }
}










