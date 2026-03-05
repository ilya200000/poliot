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
public abstract class PoliotMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onUniversalFly(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        // Чит активируется ТОЛЬКО когда элитры раскрыты (прыжок + пробел в воздухе)
        if (player.isFallFlying()) {
            // 1. Полностью отключаем ванильную гравитацию и инерцию
            player.setVelocity(0, 0, 0);

            // 2. Настройки скорости (0.5 - очень быстро для обычного мира)
            double speed = 0.5;
            Vec3d look = player.getRotationVector();
            
            double nextX = player.getX();
            double nextY = player.getY();
            double nextZ = player.getZ();

            // 3. Логика движения по направлению взгляда
            if (player.input.pressingForward) {
                nextX += look.x * speed;
                nextY += look.y * speed;
                nextZ += look.z * speed;
            }
            if (player.input.pressingBack) {
                nextX -= look.x * speed;
                nextY -= look.y * speed;
                nextZ -= look.z * speed;
            }

            // Ручное управление высотой (Пробел/Шифт)
            if (player.input.jumping) nextY += 0.3;
            if (player.input.sneaking) nextY -= 0.3;

            // 4. ПЕРЕМЕЩЕНИЕ (Самая важная часть)
            // Устанавливаем позицию клиенту
            player.setPosition(nextX, nextY, nextZ);

            // Отправляем пакет серверу, что мы ТУТ. false = мы не на земле.
            if (player.networkHandler != null) {
                player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(nextX, nextY, nextZ, false));
                
                // Каждые 5 тиков спамим пакет взлета, чтобы сервер не "забыл", что мы летим
                if (player.age % 5 == 0) {
                    player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                }
            }
        }
    }
}
