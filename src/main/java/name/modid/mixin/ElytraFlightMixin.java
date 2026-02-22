package name.modid.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
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
        if (player == null || player.networkHandler == null) return;

        // 1. АВТОВЗЛЕТ
        if (!player.isOnGround() && player.getVelocity().y < -0.1 && !player.isFallFlying()) {
            player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }

        if (player.isFallFlying()) {
            // 2. РЕЖИМ "РВАНОГО" ПОЛЕТА
            if (player.input.pressingForward) {
                Vec3d look = player.getRotationVec(1.0F);
                // Прыжок на 0.25 блока (безопасно для большинства античитов)
                double x = player.getX() + look.x * 0.25;
                double y = player.getY() + look.y * 0.25;
                double z = player.getZ() + look.z * 0.25;

                // Шлем серверу пакет: "Я уже здесь"
                player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));
                // Визуально двигаем камеру
                player.setPosition(x, y, z);
                
                // Обнуляем скорость, чтобы сервер не считал накопленную инерцию
                player.setVelocity(0, 0, 0);
            }

            // 3. ПОДДЕРЖКА ВЫСОТЫ (Space)
            if (player.input.jumping) {
                player.setPosition(player.getX(), player.getY() + 0.1, player.getZ());
                player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(player.getX(), player.getY(), player.getZ(), false));
            }
            
            // Даем серверу "успокоиться" — микропадение раз в 5 тиков
            if (player.age % 5 == 0) {
                player.addVelocity(0, -0.01, 0);
            }
        }
    }
}















