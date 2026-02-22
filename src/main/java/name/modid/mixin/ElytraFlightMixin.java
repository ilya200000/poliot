package name.modid.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
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
        if (!player.isOnGround() && player.getVelocity().y < -0.05 && !player.isFallFlying()) {
            player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }

        if (player.isFallFlying()) {
            // 2. ОБМАН СЕРВЕРА (Pitch Spoof)
            // Если зажат W, мы шлем серверу пакет, что мы якобы смотрим вверх-вниз
            if (player.input.pressingForward) {
                float fakePitch = (player.age % 10 < 5) ? -2.0F : 10.0F; 
                
                // Шлем серверу фейковый поворот головы, чтобы он сам обсчитал ускорение элитры
                player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                        player.getYaw(), 
                        fakePitch, 
                        player.isOnGround()
                ));
            }

            // 3. ЛЕГИТНЫЙ ПОДЪЕМ
            if (player.input.jumping) {
                player.addVelocity(0, 0.01, 0); 
            }
            
            // Если нас начинает сильно тянуть вниз, даем микро-импульс (безопасно)
            if (player.getVelocity().y < -0.01 && !player.input.sneaking) {
                player.addVelocity(0, 0.02, 0);
            }
        }
    }
}














