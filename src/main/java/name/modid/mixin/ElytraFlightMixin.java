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

        if (player == null || player.getWorld() == null || player.networkHandler == null) return;

        if (player.isFallFlying()) {
            Vec3d look = player.getRotationVec(1.0F);
            
            // СКОРОСТЬ 0.12 - оптимально для BedWars Mystery.
            double speed = 0.12; 

            // 1. ДВИЖЕНИЕ ВПЕРЕД (W)
            if (player.input.pressingForward) {
                // Если жмем Прыжок (Пробел) - летим ВВЕРХ
                double yBoost = player.input.jumping ? 0.08 : (look.y * speed) - 0.01;
                
                double nextX = player.getX() + look.x * speed;
                double nextY = player.getY() + yBoost;
                double nextZ = player.getZ() + look.z * speed;

                // Каждые 2 тика шлем пакет позиции
                if (player.age % 2 == 0) {
                    player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(nextX, nextY, nextZ, false));
                    player.setPosition(nextX, nextY, nextZ);
                }
            }

            // 2. ФИКС ГРАВИТАЦИИ
            // Чтобы не тянуло к полу, немного компенсируем падение, но не до нуля
            Vec3d v = player.getVelocity();
            if (!player.input.jumping) {
                player.setVelocity(v.x, -0.01, v.z);
            } else {
                player.setVelocity(v.x, 0.05, v.z);
            }
            
            player.onLanding(); // Сброс урона
        }
    }
}










