package name.modid.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.NetworkThreadUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class NetworkMixin {

    // Храним ID последнего телепорта для синхронизации в ElytraFlightMixin
    public static int lastTeleportId = -1;

    /**
     * Перехватываем пакет PlayerPositionLook (S2C).
     * Именно этот пакет заставляет тебя "тпаться" назад, когда античит недоволен.
     */
    @Inject(method = "onPlayerPositionLook", at = @At("HEAD"), cancellable = true)
    private void onPlayerPositionLook(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        ClientPlayNetworkHandler handler = (ClientPlayNetworkHandler) (Object) this;

        // Сохраняем ID телепорта
        lastTeleportId = packet.getTeleportId();

        // Если мы в полете, мы должны подтвердить телепорт немедленно, 
        // чтобы сервер разрешил следующее движение без отката.
        if (lastTeleportId != -1) {
            handler.sendPacket(new TeleportConfirmC2SPacket(lastTeleportId));
        }

        // Мы не отменяем сам пакет (ci.cancel() не нужен), 
        // чтобы клиент обновил свои координаты согласно серверу и не было рассинхрона.
    }
}

