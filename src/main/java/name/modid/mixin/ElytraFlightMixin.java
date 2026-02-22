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

        if (player == null || player.getWorld() == null) return;

        // 1. АВТО-ВЗЛЕТ (Если прыгнули - сразу летим)
        if (!player.isOnGround() && player.getVelocity().y < -0.05 && !player.isFallFlying()) {
            player.checkFallFlying();
        }

        if (player.isFallFlying()) {
            Vec3d look = player.getRotationVec(1.0F);
            
            // СКОРОСТЬ 0.08 — Безопасно для MysteryWorld. 
            // 0.15 — Максимально быстро, но может кикнуть.
            double speed = 0.085; 

            // 2. ДВИЖЕНИЕ ВПЕРЕД (W)
            if (player.input.pressingForward) {
                // Плавное ускорение (имитация фейерверка)
                player.addVelocity(look.x * speed, look.y * speed, look.z * speed);
            }

            // 3. УПРАВЛЕНИЕ ВЫСОТОЙ (Пробел / Shift)
            if (player.input.jumping) {
                // Подтяжка вверх
                player.addVelocity(0, 0.05, 0);
            } else if (player.input.sneaking) {
                // Быстрый спуск
                player.addVelocity(0, -0.15, 0);
            } else {
                // БАЙПАС ПРИТЯЖЕНИЯ:
                // Мы не даем серверу повода нас приземлить.
                // Оставляем ванильную гравитацию, но компенсируем её на 95%
                Vec3d v = player.getVelocity();
                if (v.y < -0.01) {
                    player.setVelocity(v.x, -0.005, v.z);
                }
            }

            // Защита от урона при посадке
            player.onLanding();
        }
    }
}










