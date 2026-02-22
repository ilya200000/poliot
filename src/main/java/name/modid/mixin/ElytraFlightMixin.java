package name.modid.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ElytraFlightMixin {

    private int tickCounter = 0;

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        if (player == null || player.networkHandler == null) return;

        // 1. УМНЫЙ ВЗЛЕТ
        if (!player.isOnGround() && player.getVelocity().y < -0.1 && !player.isFallFlying()) {
            player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }

        if (player.isFallFlying()) {
            tickCounter++;

            // 2. БЕСПАЛЕВНОЕ УСКОРЕНИЕ (Имитация фейерверка каждые 10 тиков)
            // Это обходит проверку на скорость, так как сервер считает это "бустом"
            if (player.input.pressingForward && tickCounter % 10 == 0) {
                // Вместо изменения Velocity напрямую, мы просто "поддерживаем" состояние полета
                // Если сервер все равно кикает, можно попробовать отправить пакет использования предмета
                player.addVelocity(player.getRotationVec(1.0F).x * 0.2, 0.1, player.getRotationVec(1.0F).z * 0.2);
            }

            // 3. ФЕЙКОВЫЙ ПАКЕТ ЖИЗНИ (Чтобы не кикало за Fly)
            if (tickCounter % 20 == 0) {
                // Отправляем серверу микро-движение, чтобы сбросить таймер "парения"
                player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, 
                        new BlockPos(player.getBlockX(), player.getBlockY() - 1, player.getBlockZ()), 
                        Direction.DOWN
                ));
            }

            // 4. УПРАВЛЕНИЕ
            if (player.input.jumping) {
                player.addVelocity(0, 0.08, 0);
            } else if (player.input.sneaking) {
                player.addVelocity(0, -0.3, 0);
            } else {
                // Вместо Hover - просто замедляем падение, но не останавливаем его совсем
                if (player.getVelocity().y < -0.05) {
                    player.addVelocity(0, 0.045, 0);
                }
            }
        }
    }
}











