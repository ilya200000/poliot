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
    private void onGrimFly(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        // Чит сработает ТОЛЬКО когда ты нажал пробел в воздухе и элитры раскрылись
        if (player.isFallFlying()) {
            // Отключаем стандартную гравитацию клиента, чтобы не тянуло вниз
            player.setVelocity(0, 0, 0);

            double speed = 0.4; // Скорость перемещения пакетами
            Vec3d look = player.getRotationVector();
            
            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();

            // Рассчитываем новую позицию на основе клавиш управления
            if (player.input.pressingForward) {
                x += look.x * speed;
                z += look.z * speed;
                y += look.y * speed; // Летит туда, куда смотришь
            }
            
            if (player.input.jumping) y += 0.2;
            if (player.input.sneaking) y -= 0.2;

            // ГЛАВНЫЙ ХАК: Шлем серверу пакет, что мы УЖЕ передвинулись
            // Grim видит это как легитимное планирование на элитрах
            if (player.networkHandler != null) {
                // Отправляем позицию напрямую в обход методов движения
                player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));
                
                // Телепортируем визуальную модель игрока в ту же точку
                player.setPosition(x, y, z);
                
                // Каждые 2 тика "перезагружаем" состояние полета для сервера
                if (player.age % 2 == 0) {
                    player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                }
            }
        }
    }
}
