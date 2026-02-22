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
        if (player != null && player.isFallFlying()) {
            Vec3d look = player.getRotationVec(1.0F);
            double speed = 0.12; // Безопасно для 1.20.1 на серверах

            // Мягкий толчок вперед
            player.addVelocity(look.x * speed, look.y * speed, look.z * speed);

            // Фикс гравитации (Bypass)
            Vec3d v = player.getVelocity();
            player.setVelocity(v.x, v.y + 0.05, v.z);
            
            player.getAbilities().allowFlying = true; // На 1.20.1 это работает стабильнее
        }
    }
}










