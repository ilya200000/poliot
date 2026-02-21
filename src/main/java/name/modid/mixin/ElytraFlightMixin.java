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
        if (player != null && player.isFallFlying()) {
            MinecraftClient client = MinecraftClient.getInstance();
            boolean isForward = client.options.forwardKey.isPressed();
            
            Vec3d look = player.getRotationVec(1.0F);
            double speed = 0.18; // Безопасная скорость для Mystery

            if (isForward) {
                // Толкаем вперед
                player.addVelocity(look.x * speed, look.y * speed, look.z * speed);
            }

            // ОБХОД АНТИЧИТА: Микро-движение вниз каждые 10 тиков
            if (player.age % 10 == 0) {
                player.addVelocity(0, -0.05, 0);
            } else {
                // В остальное время компенсируем падение
                Vec3d v = player.getVelocity();
                player.setVelocity(v.x, v.y + 0.045, v.z);
            }

            player.onLanding(); // Сброс урона
        }
    }
}








