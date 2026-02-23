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

        // 1. Безопасная проверка при заходе в мир
        if (player == null || player.isSpectator() || player.getAbilities().flying) return;

        // 2. Легитный взлет (через проверку возможности полета)
        if (!player.isOnGround() && player.getVelocity().y < -0.05 && !player.isFallFlying()) {
            player.checkFallFlying();
        }

        if (player.isFallFlying()) {
            Vec3d look = player.getRotationVec(1.0F);
            Vec3d velocity = player.getVelocity();

            // 3. СКОРОСТЬ (0.18 — это предел, который Grim считает "легальным планированием")
            // Если на твоем сервере всё равно откатывает — снизь до 0.12
            double speed = 0.18;

            // Движение вперед (W)
            if (player.input.pressingForward) {
                // Мы используем addVelocity, а не setVelocity. 
                // Это создает плавный импульс, который сервер принимает за пинг/ветер.
                double xBoost = (look.x * speed - velocity.x) * 0.2;
                double zBoost = (look.z * speed - velocity.z) * 0.2;
                player.addVelocity(xBoost, 0, zBoost);
            }

            // 4. УПРАВЛЕНИЕ ВЫСОТОЙ (Space / Shift)
            if (player.input.jumping) {
                // Поднимаем по чуть-чуть, чтобы не сработал чек на Fly
                player.addVelocity(0, 0.04, 0);
            } else if (player.input.sneaking) {
                player.addVelocity(0, -0.2, 0);
            } else {
                // ПЛАВНЫЙ ГЛАЙД (Hover)
                // Если падаем слишком быстро, мягко тормозим падение до -0.05.
                // -0.05 — это абсолютно легитимная вертикальная скорость элитры.
                if (velocity.y < -0.05) {
                    player.addVelocity(0, 0.035, 0);
                }
            }
            
            // 5. ФИКС ТРЕНИЯ
            // Не даем персонажу полностью остановиться в воздухе
            if (velocity.horizontalLength() < 0.1 && player.input.pressingForward) {
                player.addVelocity(look.x * 0.1, 0, look.z * 0.1);
            }
        }
    }
}




























