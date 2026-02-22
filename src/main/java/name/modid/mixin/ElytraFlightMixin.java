package name.modid.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ElytraFlightMixin {
    
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        
        // 1. ПРОВЕРКА НА КРАШ (Защита от Null в 1.21.11)
        if (player == null || player.getWorld() == null || player.getAbilities() == null) return;

        // 2. АВТО-ВЗЛЕТ
        if (!player.isOnGround() && player.getVelocity().y < -0.1 && !player.isFallFlying()) {
            player.checkFallFlying();
        }

        // 3. ПОЛЕТ (MysteryWorld Bypass)
        if (player.isFallFlying()) {
            Vec3d look = player.getRotationVec(1.0F);
            Vec3d v = player.getVelocity();

            // Безопасная скорость для Grim (0.05)
            double speed = 0.052;

            // Если зажат прыжок — летим вверх, иначе — глайд -0.01
            double movementY = player.isJumping() ? 0.04 : (v.y < -0.01 ? -0.01 - v.y : 0);

            player.addVelocity(look.x * speed, movementY, look.z * speed);

            // Мягкое обнуление урона
            if (player.fallDistance > 1.2f) {
                player.fallDistance = 0;
            }
        }
    }
}












