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

        // 1. АВТОВЗЛЕТ (через пакет, чтобы сервер не тупил)
        if (!player.isOnGround() && player.getVelocity().y < -0.01 && !player.isFallFlying()) {
            player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }

        if (player.isFallFlying()) {
            Vec3d look = player.getRotationVec(1.0F);
            
            // СКОРОСТЬ: 0.07 — сейвово, 0.1 — быстро. Начни с малого.
            double speed = 0.075; 

            // 2. ДВИЖЕНИЕ ВПЕРЕД (W)
            if (player.input.pressingForward) {
                Vec3d v = player.getVelocity();
                player.setVelocity(v.add(look.x * speed, look.y * speed, look.z * speed));
            }

            // 3. УПРАВЛЕНИЕ ВЫСОТОЙ (Space / Shift)
            if (player.input.jumping) {
                player.addVelocity(0, 0.05, 0);
            } else if (player.input.sneaking) {
                player.addVelocity(0, -0.2, 0);
            } else {
                // ПСЕВДО-ХОВЕР (мини-падение -0.01, чтобы не кикнуло за зависание)
                Vec3d v = player.getVelocity();
                if (v.y < -0.01) {
                    player.setVelocity(v.x, -0.01, v.z);
                }
            }
        }
    }
}










