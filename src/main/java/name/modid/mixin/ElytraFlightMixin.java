package name.modid.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
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
            
            // Если зажата W
            if (client.options.forwardKey.isPressed()) {
                Vec3d look = player.getRotationVec(1.0F);
                double speed = 0.15; // Безопасный порог для Mystery

                // Вместо Velocity меняем позицию пакетами (Bypass)
                double nextX = player.getX() + look.x * speed;
                double nextY = player.getY() + look.y * speed;
                double nextZ = player.getZ() + look.z * speed;

                // Шлем серверу пакет: "я здесь, и я на земле" (обман гравитации)
                if (player.age % 2 == 0) {
                    player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(nextX, nextY, nextZ, true, player.horizontalCollision));
                    player.setPosition(nextX, nextY, nextZ);
                }
            }

            // Фикс падения: медленное снижение, чтобы античит не кикнул за Fly
            Vec3d v = player.getVelocity();
            player.setVelocity(v.x, -0.01, v.z);
            
            player.onLanding();
        }
    }
}








