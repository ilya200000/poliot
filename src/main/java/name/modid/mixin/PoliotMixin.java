package name.modid.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ElytraFlyMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onGrimTick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        if (player.isFallFlying()) {
            // Grim очень чувствителен к оси Y. 
            // Обнуляем вертикальное падение, но оставляем микро-движение вниз (0.005)
            Vec3d vel = player.getVelocity();
            double ySpeed = 0;

            if (player.input.jumping) ySpeed = 0.05; // Медленный подъем
            else if (player.input.sneaking) ySpeed = -0.05;
            else ySpeed = -0.005; // Имитация гравитации для Grim

            // Получаем вектор направления взгляда для горизонтального полета
            Vec3d look = player.getRotationVector();
            double speed = 0.15; // Безопасная скорость для Grim. Выше 0.25 — риск кика.

            if (player.input.pressingForward) {
                // Устанавливаем плавную скорость
                player.setVelocity(look.x * speed, ySpeed, look.z * speed);
            } else {
                player.setVelocity(0, ySpeed, 0);
            }

            // ПАКЕТНЫЙ ХАК ДЛЯ GRIM:
            // Каждые 2 тика отправляем серверу пакет, что мы "снова" начали лететь.
            // Это сбивает проверки на ускорение (Prediction) у Grim.
            if (player.age % 2 == 0) {
                player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }
        }
    }
}
