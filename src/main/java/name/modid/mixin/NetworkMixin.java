package name.modid.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class NetworkMixin {
    
    public static int lastTeleportId = -1;

    @Inject(method = "onPlayerPositionLook", at = @At("HEAD"))
    private void onPlayerPositionLook(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        lastTeleportId = packet.getTeleportId();
        
        // Используем более безопасный способ отправки пакета
        ClientPlayNetworkHandler handler = (ClientPlayNetworkHandler) (Object) this;
        if (lastTeleportId != -1) {
            handler.sendPacket(new TeleportConfirmC2SPacket(lastTeleportId));
        }
    }
}
