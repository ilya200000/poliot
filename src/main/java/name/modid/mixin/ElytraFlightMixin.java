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

        // Защита от краша при загрузке
        if (player == null || player.clientWorld == null || player.input == null) return;

        // 1. АВТО-ВЗЛЕТ (Если прыгаем и падаем — активируем элитры)
        if (!player.isOnGround() && player.getVelocity().y < -0.1 && !player.isFallFlying()) {
            player.checkFallFlying();
        }

        // 2. ЛОГИКА ПОЛЕТА
        if (player.isFallFlying()) {
            Vec3d look = player.getRotationVec(1.0F);
            Vec3d v = player.getVelocity();

            // Безопасная скорость для MysteryWorld (Grim Bypass)
            double speed = 0.055; 

            // Плавный разгон вперед
            player.addVelocity(look.x * speed, 0, look.z * speed);

            // КОНТРОЛЬ ВЕРТИКАЛИ
            if (player.input.jumping) {
                // Плавный подъем на Пробел
                player.addVelocity(0, 0.045, 0);
            } else if (player.input.sneaking) {
                // Спуск на Shift
                player.addVelocity(0, -0.2, 0);
            } else {
                // ИДЕАЛЬНЫЙ ГЛАЙД (Bypass для Grim)
                // Сервер видит падение -0.01 и не флагает Fly
                if (v.y < -0.01) {
                    player.setVelocity(v.x, -0.01, v.z);
                }
            }

            // Убираем урон от падения
            player.fallDistance = 0;
        }
    }
}











