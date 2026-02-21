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

        // 1. АВТО-ВЗЛЕТ (Bypass для Grim)
        if (!player.isOnGround() && player.getVelocity().y < -0.05) {
            // Заставляем игру думать, что мы только что раскрыли элитры
            player.checkFallFlying();
        }

        if (player.isFallFlying()) {
            Vec3d look = player.getRotationVec(1.0F);
            
            // СКОРОСТЬ: 0.11 - это предел для Mystery/RW
            double s = 0.11;

            // 2. ДВИЖЕНИЕ (Имитируем планирование)
            // Мы не ставим скорость жестко, а "подруливаем" ванильную инерцию
            Vec3d v = player.getVelocity();
            player.setVelocity(
                v.x + look.x * s * 0.2, 
                -0.01, // Жестко фиксируем микро-падение для сервера
                v.z + look.z * s * 0.2
            );

            // 3. ОБХОД КИКА (Packet Reset)
            // Каждые 5 тиков мы "сбрасываем" позицию на миллиметр, чтобы античит тупил
            if (player.age % 5 == 0) {
                player.addVelocity(0, 0.02, 0);
            }

            player.onLanding(); // Защита от смерти
        }
    }
}










