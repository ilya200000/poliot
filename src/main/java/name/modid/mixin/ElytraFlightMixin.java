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

        // 1. ПРИНУДИТЕЛЬНЫЙ ВЗЛЕТ
        if (!player.isOnGround() && player.getVelocity().y < -0.05 && !player.isFallFlying()) {
            player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }

        if (player.isFallFlying()) {
            // ОСТАНАВЛИВАЕМ КЛИЕНТСКУЮ ФИЗИКУ (чтобы не было десинхрона и дерганий)
            player.setVelocity(0, 0, 0);

            // 2. РАСЧЕТ ДВИЖЕНИЯ
            // 0.25 — это предел. Если кикает, ставь 0.18
            double speed = 0.25; 
            Vec3d look = player.getRotationVec(1.0F);
            
            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();

            if (player.input.pressingForward) {
                x += look.x * speed;
                z += look.z * speed;
                y += look.y * speed;
            }
            
            if (player.input.jumping) y += 0.15;
            if (player.input.sneaking) y -= 0.15;

            // 3. БАЙПАС "GRIM": Шлем микро-пакет "падения" перед основным перемещением
            // Это обманывает проверку на полет, сервер думает, что ты падаешь
            player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(player.getX(), player.getY() - 0.005, player.getZ(), false));

            // Основной пакет перемещения
            player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(x, y, z, player.getYaw(), player.getPitch(), false));

            // Ставим игрока в новую точку визуально
            player.setPosition(x, y, z);

            // 4. ОТМЕНЯЕМ ВАНИЛЬНЫЙ ТИК (чтобы игра не тянула тебя вниз сама)
            ci.cancel();
        }
    }
}




















