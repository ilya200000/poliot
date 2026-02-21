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
        if (player == null || player.networkHandler == null) return;

        if (player.isFallFlying()) {
            MinecraftClient client = MinecraftClient.getInstance();
            
            // Проверка нажатия W (через стабильный метод)
            if (client.options.forwardKey.isPressed()) {
                Vec3d look = player.getRotationVec(1.0F);
                
                // СКОРОСТЬ: 0.12 — "Золотая середина" для 1.21.11 на серверах
                double speed = 0.12; 

                double nextX = player.getX() + look.x * speed;
                double nextY = player.getY() + (look.y * speed);
                double nextZ = player.getZ() + look.z * speed;

                // Шлем пакет с 4 аргументами (X, Y, Z, onGround)
                // Ставим false, чтобы сервер понимал, что мы в воздухе, но летим легально
                if (player.age % 2 == 0) {
                    player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(nextX, nextY, nextZ, false));
                    player.setPosition(nextX, nextY, nextZ);
                }
            }

            // ANTI-KICK: Микро-движение вниз, имитирующее планирование
            Vec3d v = player.getVelocity();
            player.setVelocity(v.x, -0.008, v.z);
            
            player.onLanding(); // Сброс урона от падения
        }
    }
}









