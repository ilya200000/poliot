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

        // 1. ПРОВЕРКА (чтобы не лагало при заходе)
        if (player == null || player.isSpectator()) return;

        // 2. АВТОВЗЛЕТ (Легитный способ через ванильный метод)
        if (!player.isOnGround() && player.getVelocity().y < -0.1 && !player.isFallFlying()) {
            player.checkFallFlying();
        }

        if (player.isFallFlying()) {
            Vec3d look = player.getRotationVec(1.0F);
            Vec3d velocity = player.getVelocity();

            // 3. СКОРОСТЬ (0.35 — это очень стабильно и быстро)
            double speed = 0.35;

            // Движение вперед (W)
            if (player.input.pressingForward) {
                // Мы просто "подменяем" текущую скорость на скорость по взгляду
                // БЕЗ ручной отправки пакетов (клиент сам их отправит в конце тика)
                player.setVelocity(look.x * speed, velocity.y, look.z * speed);
            }

            // 4. УПРАВЛЕНИЕ ВЫСОТОЙ
            if (player.input.jumping) {
                // Плавный подъем
                player.addVelocity(0, 0.05, 0);
            } else if (player.input.sneaking) {
                // Быстрый спуск
                player.setVelocity(velocity.x, -0.5, velocity.z);
            } else {
                // БАЙПАС ПАДЕНИЯ (Hover)
                // -0.01 — магическое число, падение почти незаметно
                if (player.getVelocity().y < -0.01) {
                    player.setVelocity(player.getVelocity().x, -0.01, player.getVelocity().z);
                }
            }
        }
    }
}



























