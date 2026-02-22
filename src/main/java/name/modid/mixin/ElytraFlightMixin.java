package name.modid.mixin;

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

        // Полная защита от вылетов на 1.20.1
        if (player == null || player.getWorld() == null || player.networkHandler == null) return;

        if (player.isFallFlying()) {
            Vec3d look = player.getRotationVec(1.0F);
            
            // СКОРОСТЬ 0.11 - предел для BedWars Mystery. Выше - откинет назад.
            double speed = 0.11; 

            if (player.input.pressingForward) {
                // ОБХОД ПРИТЯЖЕНИЯ (Grim Bypass)
                // Каждые 2 тика шлем серверу пакет: "я здесь и я легально падаю"
                if (player.age % 2 == 0) {
                    double nextX = player.getX() + look.x * speed;
                    double nextY = player.getY() + (look.y * speed) - 0.035; // Имитация падения (Y вниз)
                    double nextZ = player.getZ() + look.z * speed;

                    // Для 1.20.1: конструктор принимает 4 аргумента (x, y, z, onGround)
                    player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(nextX, nextY, nextZ, false));
                    player.setPosition(nextX, nextY, nextZ);
                }
            }

            // Плавное визуальное снижение
            Vec3d v = player.getVelocity();
            player.setVelocity(v.x, -0.015, v.z);
            
            player.onLanding(); // Сброс урона
        }
    }
}










