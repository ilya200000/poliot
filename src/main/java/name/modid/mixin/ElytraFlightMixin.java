package name.modid.mixin;

import net.minecraft.client.MinecraftClient;
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

        // Летаем только если элитры реально активированы
        if (player.isFallFlying()) {
            // Прямое обращение к кнопкам через настройки игры (самый стабильный способ)
            boolean isForwardPressed = MinecraftClient.getInstance().options.forwardKey.isPressed();
            boolean isJumpPressed = MinecraftClient.getInstance().options.jumpKey.isPressed();

            Vec3d look = player.getRotationVec(1.0F);
            double speed = 0.4; // Твоя скорость полета

            if (isForwardPressed) {
                // Плавное ускорение вперед по направлению взгляда
                player.addVelocity(look.x * speed, look.y * speed, look.z * speed);
            } else {
                // Если W не нажата — зависаем (медленное планирование)
                Vec3d v = player.getVelocity();
                player.setVelocity(v.x, -0.005, v.z);
            }

            // Набор высоты на пробел
            if (isJumpPressed) {
                player.addVelocity(0, 0.15, 0);
            }

            // Убираем урон при приземлении
            player.fallDistance = 0;
        }
    }
}





