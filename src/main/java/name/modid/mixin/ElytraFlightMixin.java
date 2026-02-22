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

    private int teleportId = 0;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        if (player == null || player.networkHandler == null) return;

        // 1. АВТОВЗЛЕТ (Force Start)
        if (!player.isOnGround() && player.getVelocity().y < -0.01 && !player.isFallFlying()) {
            player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }

        if (player.isFallFlying()) {
            // 2. ОТКЛЮЧАЕМ ВАНИЛЬНУЮ ФИЗИКУ (Grim Bypass)
            // Мы сами будем слать пакеты движения, минуя расчеты клиента
            Vec3d look = player.getRotationVec(1.0F);
            double speed = 0.15; // Начни с 0.15. Если не кикает - ставь 0.25

            double nextX = player.getX();
            double nextY = player.getY();
            double nextZ = player.getZ();

            if (player.input.pressingForward) {
                nextX += look.x * speed;
                nextZ += look.z * speed;
                nextY += look.y * speed;
            }
            if (player.input.jumping) nextY += speed / 2;
            if (player.input.sneaking) nextY -= speed / 2;

            // 3. ПАКЕТНЫЙ ХАК: Шлем пакет "Я стою", потом пакет "Я лечу"
            // Это сбивает с толку систему предсказаний (Prediction)
            player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(player.getX(), player.getY(), player.getZ(), true));
            
            // Основной пакет движения (ставим OnGround = false, чтобы сервер видел полет)
            player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                nextX, nextY, nextZ, 
                player.getYaw(), player.getPitch(), 
                false
            ));

            // Синхронизируем камеру, чтобы не было тряски
            player.setPosition(nextX, nextY, nextZ);
            player.setVelocity(0, 0, 0); // Обнуляем реальную скорость

            // 4. ОТМЕНЯЕМ ВАНИЛЬНЫЙ ТИК
            // Игра не будет тянуть тебя вниз, пока работает этот код
            ci.cancel();
        }
    }
}
















