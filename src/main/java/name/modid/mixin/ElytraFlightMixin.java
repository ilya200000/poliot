package name.modid.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ElytraFlightMixin {
    
    @Inject(method = "tick", at = @At("HEAD")) // Используем HEAD для стабильности
    private void onTick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        
        // Базовая проверка, чтобы не крашнуло в меню
        if (player == null || player.getAbilities() == null) return;

        // 1. АВТО-ВЗЛЕТ (Если падаем без элитр)
        if (!player.isOnGround() && player.getVelocity().y < -0.1 && !player.isFallFlying()) {
            player.checkFallFlying();
        }

        // 2. ЛОГИКА ПОЛЕТА
        if (player.isFallFlying()) {
            Vec3d look = player.getRotationVec(1.0F);
            Vec3d v = player.getVelocity();

            // Безопасная скорость для MysteryWorld
            double speed = 0.05; 

            // Движение вперед (добавляем к текущему вектору)
            double mx = look.x * speed;
            double mz = look.z * speed;
            double my = 0;

            // Управление высотой через ванильные кнопки
            if (player.input.jumping) {
                my = 0.03; // Вверх
            } else if (player.input.sneaking) {
                my = -0.15; // Вниз
            } else {
                // Глайд-режим (анти-гравитация)
                if (v.y < -0.01) {
                    my = 0.008; // Чуть подталкиваем вверх, чтобы падение было -0.01
                }
            }

            // Устанавливаем скорость напрямую через поля (самый стабильный способ)
            player.addVelocity(mx, my, mz);

            // 3. ФИКС УРОНА (Чтобы не разбиться при посадке)
            if (player.fallDistance > 1.0f) {
                player.fallDistance = 0;
            }
        }
    }
}













