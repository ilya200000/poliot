package name.modid.mixin;

import net.minecraft.client.MinecraftClient;
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
        if (player == null || player.networkHandler == null) return;

        if (player.isFallFlying()) {
            MinecraftClient client = MinecraftClient.getInstance();
            
            // Если жмем W
            if (client.options.forwardKey.isPressed()) {
                Vec3d look = player.getRotationVec(1.0F);
                
                // СКОРОСТЬ 0.14 - это предел для Grim Anticheat
                double s = 0.14;

                // Вместо addVelocity (которое палится), мы "телепортируем" пакет
                // Каждые 2 тика шлем серверу пакет: "я здесь и я падаю"
                if (player.age % 2 == 0) {
                    double x = player.getX() + look.x * s;
                    double y = player.getY() + (look.y * s) - 0.02; // Имитируем падение (Y вниз)
                    double z = player.getZ() + look.z * s;

                    // Шлем пакет позиции (в 1.21.11 ровно 4 аргумента: x, y, z, onGround)
                    player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));
                    player.setPosition(x, y, z);
                }
            }

            // Визуальное падение для сервера (чтобы не кикнуло за Fly)
            player.setVelocity(player.getVelocity().x, -0.01, player.getVelocity().z);
            player.onLanding();
        }
    }
}









