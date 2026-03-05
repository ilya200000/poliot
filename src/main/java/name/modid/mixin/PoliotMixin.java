package name.modid.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class PoliotMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onElytraFlyTick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

        // Работает только если элитры уже раскрыты (Fall Flying)
        if (player.isFallFlying()) {
            Vec3d look = player.getRotationVector();
            double speed = 0.15; // Максимально безопасная скорость для Grim
            double ySpeed;

            // Управление вертикалью (Пробел - вверх, Шифт - вниз)
            if (player.input.jumping) {
                ySpeed = 0.05;
            } else if (player.input.sneaking) {
                ySpeed = -0.05;
            } else {
                // Константное микро-падение, чтобы Grim не флагнул за "зависание"
                ySpeed = -0.005;
            }

            // Движение вперед (клавиша W)
            if (player.input.pressingForward) {
                player.setVelocity(look.x * speed, ySpeed, look.z * speed);
            } else {
                // Если W не зажата, просто плавно опускаемся
                player.setVelocity(0, ySpeed, 0);
            }

            // ГЛАВНЫЙ ОБХОД: Каждые 2 тика шлем пакет "Начал полет на элитрах"
            // Это сбивает предикцию античита и позволяет лететь горизонтально
            if (player.age % 2 == 0 && player.networkHandler != null) {
                player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }
        }
    }
}
