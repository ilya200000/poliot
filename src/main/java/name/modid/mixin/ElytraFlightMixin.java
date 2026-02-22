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
            Vec3d look = player.getRotationVec(1.0F);
            
            // СКОРОСТЬ: 0.15 — лимит для большинства серверов без кика. 
            // Если всё еще тпает — ставь 0.1
            double speed = 0.15; 
            
            // ВМЕСТО ty += look.y мы делаем микро-падение
            // Это "успокаивает" античит, так как ты технически ПАДАЕШЬ
            double tx = player.getX();
            double ty = player.getY() - 0.04; // Константное падение (легит)
            double tz = player.getZ();

            if (player.input.pressingForward) {
                tx += look.x * speed;
                tz += look.z * speed;
                // При движении вперед чуть-чуть компенсируем падение
                ty += 0.035; 
            }

            if (player.input.jumping) ty += 0.1; // Плавный подъем
            if (player.input.sneaking) ty -= 0.3; // Быстрый спуск

            // ХАК ДЛЯ СИНХРОНИЗАЦИИ:
            // Шлем серверу подтверждение, что мы НЕ на земле
            player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(tx, ty, tz, false));
            
            // Принудительно ставим позицию на клиенте
            player.setPosition(tx, ty, tz);
            player.setVelocity(0, 0, 0);

            // Отменяем ванильный тик, чтобы он не конфликтовал с нашими координатами
            ci.cancel();
        }
    }
}

















