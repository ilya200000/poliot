package name.modid.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ClientPlayerEntity.class)
public abstract class ElytraFlightMixin {

    private final List<PlayerMoveC2SPacket> packetQueue = new ArrayList<>();
    private int teleportId = 0;
    private int tickCounter = 0;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        if (player == null || player.networkHandler == null) return;

        // 1. ПРИНУДИТЕЛЬНЫЙ ВЗЛЕТ (Force Server-Side State)
        if (!player.isOnGround() && player.getVelocity().y < -0.05 && !player.isFallFlying()) {
            player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }

        if (player.isFallFlying()) {
            tickCounter++;

            // 2. ОБНУЛЕНИЕ ВАНИЛЬНОЙ ФИЗИКИ (Prevent Desync)
            player.setVelocity(0, 0, 0);
            
            double speed = 0.28; // Максимально стабильная скорость для Grim 1.20.1
            Vec3d look = player.getRotationVec(1.0F);
            
            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();

            // 3. УПРАВЛЕНИЕ (Vector calculation)
            if (player.input.pressingForward) {
                x += look.x * speed;
                z += look.z * speed;
                y += look.y * speed;
            }
            if (player.input.jumping) y += 0.2;
            if (player.input.sneaking) y -= 0.2;

            // 4. GRIM BYPASS (Collision Spoof)
            // Мы шлем пакет "я упал в блок", чтобы сбросить проверку движения
            player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y - 0.0001, z, false));

            // 5. PACKET SENDING (Main Movement)
            // Шлем пакет "Full" для синхронизации Yaw/Pitch
            player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                x, y, z, player.getYaw(), player.getPitch(), false
            ));

            // 6. ТЕЛЕПОРТАЦИЯ МОДЕЛЬКИ (Client-side sync)
            player.setPosition(x, y, z);

            // 7. ANTI-KICK (Keep-alive)
            // Раз в 20 тиков шлем пакет "на земле", чтобы сервер не считал нас парящими вечно
            if (tickCounter % 20 == 0) {
                player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, true));
            }

            // ПОЛНОСТЬЮ ОТМЕНЯЕМ ВАНИЛЬНЫЙ ТИК
            ci.cancel();
        }
    }
}






















