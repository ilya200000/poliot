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
        if (player == null || player.networkHandler == null) return;

        // 1. АВТО-АКТИВАЦИЯ (Для захода на полет без фейерверка)
        if (!player.isOnGround() && player.getVelocity().y < -0.01 && !player.isFallFlying()) {
            player.checkFallFlying();
        }

        if (player.isFallFlying()) {
            Vec3d look = player.getRotationVec(1.0F);
            Vec3d velocity = player.getVelocity();

            // СКОРОСТЬ ДЛЯ MYSTERYWORLD (Безопасный порог 0.04 - 0.06)
            double speed = 0.052; 

            // 2. УМНОЕ УСКОРЕНИЕ (Не ломает вектор движения)
            // Мы просто чуть-чуть подталкиваем игрока вперед
            player.addVelocity(
                look.x * speed,
                0, // По Y не трогаем, чтобы не было флагов за Fly
                look.z * speed
            );

            // 3. АНТИ-ПАДЕНИЕ (Замедление гравитации)
            // Если мы начинаем падать слишком быстро, замедляем вертикальную скорость
            if (velocity.y < -0.05) {
                player.setVelocity(player.getVelocity().x, -0.02, player.getVelocity().z);
            }

            // 4. ВЗЛЕТ НА ПРОБЕЛ (Имитация набора высоты)
            if (player.input.jumping) {
                player.addVelocity(0, 0.04, 0);
            }

            // Сброс дистанции падения, чтобы не разбиться
            if (player.fallDistance > 2) {
                player.onLanding();
            }
        }
    }
}











