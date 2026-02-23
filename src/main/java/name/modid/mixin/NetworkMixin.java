package name.modid.mixin;

import name.modid.ElytraData;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class NetworkMixin {

    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (ElytraData.isFlying && packet instanceof PlayerMoveC2SPacket) {
            // Складываем пакеты движения в очередь вместо отправки
            ElytraData.packetQueue.add(packet);
            ci.cancel();
        }
    }

    @Inject(method = "onPlayerPositionLook", at = @At("HEAD"))
    private void onTeleport(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        ElytraData.lastTeleportId = packet.getTeleportId();
        ClientPlayNetworkHandler handler = (ClientPlayNetworkHandler) (Object) this;
        handler.sendPacket(new TeleportConfirmC2SPacket(ElytraData.lastTeleportId));
    }
}
