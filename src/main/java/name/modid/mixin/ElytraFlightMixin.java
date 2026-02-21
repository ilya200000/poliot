package name.modid.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class ElytraFlightMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (player.getWorld().isClient && player.isFallFlying()) {
            // 1. Убираем стандартное замедление (парение)
            player.getAbilities().flying = true;

            // 2. Движение ВПЕРЕД (куда смотришь)
            // Берем вектор взгляда
            Vec3d look = player.getRotationVec(1.0F);
            double speed = 0.35; // Скорость полета (0.35 - оптимально для серверов)

            // Применяем скорость только если нажаты кнопки движения
            if (player.forwardSpeed > 0) {
                player.setVelocity(look.x * speed, look.y * speed, look.z * speed);
            }

            // 3. ANTI-KICK (Микро-падение)
            // Чтобы сервер не кикал за полет, мы заставляем персонажа чуть-чуть 
            // опускаться вниз каждые несколько тиков (имитация падения)
            if (player.age % 20 == 0) {
                player.addVelocity(0, -0.01, 0);
            }
        }
    }
}




