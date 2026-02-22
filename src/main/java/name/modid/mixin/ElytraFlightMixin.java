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

        // ИСПРАВЛЕНО: world вместо clientWorld для 1.20.1
        if (player == null || player.getWorld() == null || player.input == null) return;

        // 1. АВТО-ВЗЛЕТ (Bypass для MysteryWorld)
        if (!player.isOnGround() && player.getVelocity().y < -0.1 && !player.isFallFlying()) {
            player.checkFallFlying();
        }

        if (player.isFallFlying()) {
            Vec3d look = player.getRotationVec(1.0F);
            Vec3d v = player.getVelocity();

            // --- НАСТРОЙКИ (MysteryWorld / Grim) ---
            // 0.058 - идеальный буст, не палится античитом
            double boostPower = 0.058; 
            double maxSpeed = 0.7; 

            // 2. УСКОРЕНИЕ (W)
            if (player.input.pressingForward) {
                if (v.horizontalLength() < maxSpeed) {
                    player.addVelocity(
                        look.x * boostPower,
                        look.y * boostPower * 0.5, 
                        look.z * boostPower
                    );
                }
            }

            // 3. УПРАВЛЕНИЕ (Пробел / Shift)
            if (player.input.jumping) {
                player.addVelocity(0, 0.05, 0); // Вверх
            } else if (player.input.sneaking) {
                player.addVelocity(0, -0.2, 0); // Вниз
            } else {
                // БАЙПАС: Фиксируем падение на -0.01 (сервер видит планирование)
                if (v.y < -0.01) {
                    player.setVelocity(v.x, -0.01, v.z);
                }
            }

            // 4. БЕЗОПАСНОСТЬ
            player.onLanding(); // Сброс урона (безопаснее чем fallDistance = 0)
        }
    }
}











