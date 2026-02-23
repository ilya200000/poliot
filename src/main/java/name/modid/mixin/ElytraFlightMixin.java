package name.modid.mixin;

import name.modid.ElytraData;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ElytraFlightMixin {

    private int tickCounter = 0;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        if (player == null || player.networkHandler == null) return;

        if (!player.isOnGround() && player.getVelocity().y < -0.05 && !player.isFallFlying()) {
            player.checkFallFlying();
        }

        if (player.isFallFlying()) {
            ElytraData.isFlying = true;
            tickCounter++;

            double speed = 0.25; // Профессиональная скорость для Grim
            Vec3d look = player.getRotationVec(1.0F);

            // Имитируем движение на клиенте
            if (player.input.pressingForward) {
                player.setPos(player.getX() + look.x * speed, player.getY() + look.y * speed, player.getZ() + look.z * speed);
            }
            if (player.input.jumping) player.setPos(player.getX(), player.getY() + 0.1, player.getZ());

            // Каждые 3 тика "выстреливаем" накопленные пакеты
            if (tickCounter % 3 == 0) {
                while (!ElytraData.packetQueue.isEmpty()) {
                    Packet<?> p = ElytraData.packetQueue.poll();
                    if (p != null) player.networkHandler.getPacketBundleHandler(); // Заглушка для конвейера
                    player.networkHandler.sendPacket(p);
                }
            }
        } else {
            ElytraData.isFlying = false;
            // Очистка очереди при приземлении
            ElytraData.packetQueue.clear();
        }
    }
}
