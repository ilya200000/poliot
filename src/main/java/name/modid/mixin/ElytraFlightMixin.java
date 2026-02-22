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

            // 2. BOUNCE / CONSTANT SPEED
            // Мы берем текущую горизонтальную скорость и не даем ей упасть
            double currentSpeed = Math.sqrt(v.x * v.x + v.z * v.z);
            double targetSpeed = 0.5; // Базовая скорость (чуть выше обычной элитры)

            if (player.input.pressingForward) {
                // Если мы замедляемся — подталкиваем до targetSpeed
                if (currentSpeed < targetSpeed) {
                    player.setVelocity(look.x * targetSpeed, v.y, look.z * targetSpeed);
                }
            }

            // 3. GLIDE (Отмена падения)
            // Вместо 0.0 (за что кикают), ставим -0.01. Ты падаешь на 1 блок за 100 секунд.
            if (!player.input.jumping && !player.input.sneaking) {
                if (v.y < -0.01) {
                    player.setVelocity(player.getVelocity().x, -0.01, player.getVelocity().z);
                }
            }

            // 4. УПРАВЛЕНИЕ
            if (player.input.jumping) {
                player.addVelocity(0, 0.05, 0); // Плавный набор высоты
            }
            if (player.input.sneaking) {
                player.addVelocity(0, -0.2, 0); // Спуск
            }
        }
    }
}













