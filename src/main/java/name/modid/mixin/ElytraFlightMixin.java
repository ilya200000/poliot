package name.modid.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ElytraFlightMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        if (player == null || player.networkHandler == null) return;

        // 1. ВЗЛЕТ (Если падаем - открываем)
        if (!player.isOnGround() && player.getVelocity().y < -0.08 && !player.isFallFlying()) {
            player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }

        if (player.isFallFlying()) {
            Vec3d v = player.getVelocity();
            Vec3d look = player.getRotationVec(1.0F);

            // 2. БАЙПАС ТРЕНИЯ (БЕЗ УСТАНОВКИ СКОРОСТИ)
            // Вместо setVelocity мы используем addVelocity, чтобы Grim не видел резких скачков.
            // 0.421 - это ванильная скорость планирования. Сервер ее не трогает.
            if (player.input.pressingForward) {
                double speed = v.horizontalLength();
                if (speed < 0.42) {
                    // Добавляем микро-импульс, чтобы компенсировать трение
                    player.addVelocity(look.x * 0.025, 0, look.z * 0.025);
                }
            }

            // 3. БАЙПАС ГРАВИТАЦИИ (Глайд)
            // Если падаем быстрее -0.05, подталкиваем вверх. 
            // -0.05 - это легитное падение, за которое НЕ откидывает.
            if (!player.input.jumping && !player.input.sneaking) {
                if (v.y < -0.05) {
                    player.setVelocity(v.x, -0.05, v.z);
                }
            }

            // 4. ПОДЪЕМ (Пробел)
            if (player.input.jumping) {
                player.addVelocity(0, 0.04, 0);
            }
        }
    }
}

























