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

        // Летаем только если элитры реально раскрыты
        if (player != null && player.isFallFlying()) {
            
            // Читаем нажатие кнопок напрямую из настроек (самый стабильный способ)
            boolean isForward = MinecraftClient.getInstance().options.forwardKey.isPressed();
            boolean isJump = MinecraftClient.getInstance().options.jumpKey.isPressed();

            Vec3d look = player.getRotationVec(1.0F);
            double speed = 0.25; // Оптимально для MysteryWorld (0.1 - 0.3)

            if (isForward) {
                // Плавное ускорение вперед
                player.addVelocity(look.x * speed, look.y * speed, look.z * speed);
            } else {
                // Режим парения (Anti-Gravity)
                Vec3d v = player.getVelocity();
                player.setVelocity(v.x, -0.005, v.z);
            }

            // Набор высоты на Пробел
            if (isJump) {
                player.addVelocity(0, 0.1, 0);
            }

            // Убираем урон при посадке
            player.fallDistance = 0;
        }
    }
}






