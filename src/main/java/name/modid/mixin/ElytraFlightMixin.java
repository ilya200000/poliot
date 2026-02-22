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
            player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }

        if (player.isFallFlying()) {
            Vec3d v = player.getVelocity();
            Vec3d look = player.getRotationVec(1.0F);

            // 2. ПОДДЕРЖКА СКОРОСТИ (W)
            // Мы не СТАВИМ скорость, а УМНОЖАЕМ текущую. 
            // 1.01 — это микро-буст, который Grim списывает на погрешность вычислений.
            if (player.input.pressingForward) {
                double speed = Math.sqrt(v.x * v.x + v.z * v.z);
                if (speed < 0.5) { // 0.5 — безопасный порог
                    player.setVelocity(v.x * 1.01, v.y, v.z * 1.01);
                }
            }

            // 3. БАЙПАС ГРАВИТАЦИИ (Глайд)
            // Мы "подтягиваем" игрока вверх только если он реально падает.
            // -0.05 — это легитное падение. -0.01 часто палится.
            if (!player.input.jumping && !player.input.sneaking) {
                if (v.y < -0.05) {
                    player.setVelocity(v.x, -0.05, v.z);
                }
            }

            // 4. ПОДЪЕМ (Только Пробел)
            if (player.input.jumping) {
                player.addVelocity(0, 0.04, 0);
            }
        }
    }
}





















