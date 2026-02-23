package name.modid.mixin;

import name.modid.ElytraData;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.Packet;
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

        // Авто-взлет (через ванильный метод для легитности)
        if (!player.isOnGround() && player.getVelocity().y < -0.05 && !player.isFallFlying()) {
            player.checkFallFlying();
        }

        if (player.isFallFlying()) {
            ElytraData.isFlying = true;
            tickCounter++;

            // СКОРОСТЬ (0.22 — база для обхода Grim)
            double speed = 0.22; 
            Vec3d look = player.getRotationVec(1.0F);

            // 1. ДВИЖЕНИЕ НА КЛИЕНТЕ (Чтобы не было дерганий экрана)
            if (player.input.pressingForward) {
                player.setPos(player.getX() + look.x * speed, player.getY() + look.y * speed, player.getZ() + look.z * speed);
            }
            if (player.input.jumping) player.setPos(player.getX(), player.getY() + 0.1, player.getZ());
            
            // Фиксируем скорость клиента в 0, чтобы ванильная физика не мешала "блинку"
            player.setVelocity(0, 0, 0);

            // 2. ВЫСТРЕЛ ПАКЕТАМИ (Blink Bypass)
            // Каждые 3 тика отправляем всё, что накопил NetworkMixin
            if (tickCounter % 3 == 0) {
                while (!ElytraData.packetQueue.isEmpty()) {
                    Packet<?> p = ElytraData.packetQueue.poll();
                    if (p != null) {
                        // Шлем пакет напрямую без лишних проверок
                        player.networkHandler.getConnection().send(p);
                    }
                }
            }
        } else {
            ElytraData.isFlying = false;
            // Чистим очередь, если упали или приземлились
            if (!ElytraData.packetQueue.isEmpty()) {
                ElytraData.packetQueue.clear();
            }
        }
    }
}

