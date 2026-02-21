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

        // Летаем только если элитры активированы
        if (player.isFallFlying()) {
            Vec3d look = player.getRotationVec(1.0F);
            double speed = 0.6; // Скорость полета

            // Летим вперед при нажатии W
            if (player.input.pressingForward) {
                player.setVelocity(look.x * speed, look.y * speed, look.z * speed);
            } else {
                // Зависание (медленное планирование)
                player.setVelocity(player.getVelocity().x, -0.01, player.getVelocity().z);
            }
            
            // Защита от смерти при падении
            player.onLanding();
        }
    }
}





