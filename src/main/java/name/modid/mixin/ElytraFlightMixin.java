package name.modid.mixin;

import name.modid.ElytraData;
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
            ElytraData.tickCounter++;
            
            // Замораживаем клиентскую физику
            player.setVelocity(0, 0.005, 0); 

            // СКОРОСТЬ: 0.2 — это край. Если откидывает — ставь 0.15
            double speed = 0.2; 
            Vec3d look = player.getRotationVec(1.0F);

            // РЕЖИМ БЛИНКА: Шлем пакеты только каждый 3-й тик
            // Это "лагание" обходит Prediction у Grim
            if (ElytraData.tickCounter % 3 == 0) {
                double x = player.getX() + (player.input.pressingForward ? look.x * speed * 3 : 0);
                double y = player.getY() + (player.input.jumping ? 0.4 : (player.input.sneaking ? -0.5 : -0.02));
                double z = player.getZ() + (player.input.pressingForward ? look.z * speed * 3 : 0);

                // Шлем ПРОВЕРКУ коллизии (сбиваем античит)
                player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y - 0.01, z, false));
                
                // Основной пакет движения
                player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(x, y, z, player.getYaw(), player.getPitch(), false));
                
                player.setPosition(x, y, z);
            }

            // Отменяем стандартный тик, чтобы не было rubberbanding от клиента
            ci.cancel();
        }
    }
}
























