package name.modid.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ElytraFlightMixin {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        if (player == null || player.networkHandler == null) return;

        if (!player.isOnGround() && player.getVelocity().y < -0.05 && !player.isFallFlying()) {
            player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }

        if (player.isFallFlying()) {
            player.setVelocity(0, 0, 0);

            double speed = 0.18; // Сейвовая скорость
            Vec3d look = player.getRotationVec(1.0F);
            
            double x = player.getX() + (player.input.pressingForward ? look.x * speed : 0);
            double y = player.getY() + (player.input.jumping ? 0.12 : (player.input.sneaking ? -0.2 : -0.002));
            double z = player.getZ() + (player.input.pressingForward ? look.z * speed : 0);

            // Используем метод из NetworkMixin
            if (NetworkMixin.getLastId() != -1) {
                player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(x, y, z, player.getYaw(), player.getPitch(), false));
            }
            
            player.setPosition(x, y, z);
            ci.cancel();
        }
    }
}























