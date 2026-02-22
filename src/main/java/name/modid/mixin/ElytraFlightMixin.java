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
    private int ticks = 0;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        if (player == null || player.networkHandler == null) return;

        if (!player.isOnGround() && !player.isFallFlying()) {
             player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }

        if (player.isFallFlying()) {
            ticks++;
            // РАЗ В 4 ТИКА ДЕЛАЕМ "РЫВОК" (Имитация лага)
            if (ticks % 4 == 0) {
                double s = 0.4; // Скорость рывка
                var look = player.getRotationVec(1.0F);
                double x = player.getX() + (player.input.pressingForward ? look.x * s : 0);
                double z = player.getZ() + (player.input.pressingForward ? look.z * s : 0);
                double y = player.getY() + (player.input.jumping ? 0.2 : -0.04);

                // Шлем один "жирный" пакет вместо кучи мелких
                player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));
                player.setPosition(x, y, z);
            }
            
            player.setVelocity(0, 0, 0); // Замораживаем ванильную дрисню
            ci.cancel(); // Убиваем ванильный тик
        }
    }
}


















