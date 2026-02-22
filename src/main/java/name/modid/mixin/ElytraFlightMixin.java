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
        if (!player.isOnGround() && player.getVelocity().y < -0.05 && !player.isFallFlying()) {
            player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }

        if (player.isFallFlying()) {
            Vec3d look = player.getRotationVec(1.0F);
            
            // 2. БЕЗОПАСНАЯ СКОРОСТЬ (0.23 — Grim обычно не дергает на этом значении)
            double speed = 0.23; 

            if (player.input.pressingForward) {
                // Плавная установка скорости, чтобы сервер успевал за тобой
                player.setVelocity(look.x * speed, player.getVelocity().y, look.z * speed);
            }

            // 3. УПРАВЛЕНИЕ ВЫСОТОЙ
            if (player.input.jumping) {
                // Поднимаем по чуть-чуть (0.03), чтобы не кикнуло за Fly
                player.addVelocity(0, 0.03, 0);
            } else if (player.input.sneaking) {
                player.setVelocity(player.getVelocity().x, -0.3, player.getVelocity().z);
            } else {
                // БАЙПАС ПАДЕНИЯ
                // Вместо -0.01 ставим -0.04. Это чуть быстрее, но убирает "застревание" и ТП вверх.
                if (player.getVelocity().y < -0.04) {
                    player.setVelocity(player.getVelocity().x, -0.04, player.getVelocity().z);
                }
            }
        }
    }
}



















