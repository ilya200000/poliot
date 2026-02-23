package name.modid.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
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
        if (player == null || player.networkHandler == null) return;

        // 1. АВТОВЗЛЕТ
        if (!player.isOnGround() && player.getVelocity().y < -0.1 && !player.isFallFlying()) {
            player.checkFallFlying();
        }

        if (player.isFallFlying()) {
            Vec3d v = player.getVelocity();
            Vec3d look = player.getRotationVec(1.0F);

            // 2. БЕЗОПАСНЫЙ БУСТ (W)
            // Мы не СТАВИМ скорость, мы ее СОХРАНЯЕМ.
            // 0.42 - это ванильная скорость планирования. Если ее держать - это не чит.
            if (player.input.pressingForward) {
                double currentSpeed = Math.sqrt(v.x * v.x + v.z * v.z);
                if (currentSpeed < 0.42) {
                    player.addVelocity(look.x * 0.02, 0, look.z * 0.02);
                }
            }

            // 3. БАЙПАС ПАДЕНИЯ (Глайд)
            // Вместо -0.01 (дрисня, которую палят), ставим -0.04. 
            // Это 1 блок падения за 5 секунд. Почти бесконечно.
            if (!player.input.jumping && !player.input.sneaking) {
                if (v.y < -0.04) {
                    // Мягко подталкиваем вверх, чтобы не падать быстро
                    player.addVelocity(0, 0.035, 0);
                }
            }

            // 4. ПОДЪЕМ (Space)
            if (player.input.jumping) {
                player.addVelocity(0, 0.04, 0);
            }
        }
    }
}
