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
            
            // Если зажата кнопка Вперед (W)
            if (client.options.forwardKey.isPressed()) {
                Vec3d look = player.getRotationVec(1.0F);
                
                // СКОРОСТЬ: 0.15 — идеальный баланс для MysteryWorld. 
                // Выше 0.2 будет кикать "Speed (A)".
                double s = 0.15;

                // Вместо addVelocity используем мягкий импульс
                Vec3d velocity = player.getVelocity();
                player.setVelocity(
                    velocity.x + look.x * s + (look.x * 1.0 - velocity.x) * 0.1,
                    velocity.y + look.y * s + (look.y * 1.0 - velocity.y) * 0.1,
                    velocity.z + look.z * s + (look.z * 1.0 - velocity.z) * 0.1
                );
            }

            // ANTI-KICK: Имитируем небольшое падение каждые 5 тиков
            if (player.age % 5 == 0) {
                player.addVelocity(0, -0.02, 0);
            }

            // Сброс дистанции падения, чтобы не разбиться
            player.onLanding();
        }
    }
}










