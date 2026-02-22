package name.modid.mixin;

import name.modid.ElytraData;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class NetworkMixin {

    @Inject(method = "onPlayerPositionLook", at = @At("HEAD"))
    private void onPlayerPositionLook(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        ElytraData.lastTeleportId = packet.getTeleportId();
        
        ClientPlayNetworkHandler handler = (ClientPlayNetworkHandler) (Object) this;
        if (ElytraData.lastTeleportId != -1) {
            handler.sendPacket(new TeleportConfirmC2SPacket(ElytraData.lastTeleportId));
        }
    }
}


