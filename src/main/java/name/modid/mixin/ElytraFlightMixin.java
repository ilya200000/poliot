package name.modid.mixin;

import net.minecraft.client.MinecraftClient;
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

        // 1. АВТО-ВЗЛЕТ (Bypass для Mystery)
        // Если мы падаем, мод сам шлет пакет серверу "я раскрыл элитры"
        if (!player.isOnGround() && !player.isFallFlying() && player.getVelocity().y < -0.1) {
            player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }

        // 2. УМНЫЙ ПОЛЕТ
        if (player.isFallFlying()) {
            MinecraftClient client = MinecraftClient.getInstance();
            Vec3d look = player.getRotationVec(1.0F);
            
            // СКОРОСТЬ: 0.08 - это очень мало, но это НЕ ПАЛИТСЯ.
            // Ты будешь лететь вечно и не падать.
            double s = 0.08; 

            if (client.options.forwardKey.isPressed()) {
                // Вместо жесткой установки скорости, мы плавно "подруливаем"
                Vec3d v = player.getVelocity();
                player.setVelocity(v.x + look.x * s, v.y + look.y * s + 0.05, v.z + look.z * s);
            }

            // 3. ОБМАН ГРАВИТАЦИИ (Anti-Kick)
            // Каждые 2 тика мы имитируем падение, чтобы сервер не считал это Fly
            Vec3d vel = player.getVelocity();
            if (player.age % 2 == 0) {
                player.setVelocity(vel.x, -0.01, vel.z);
            }

            player.onLanding(); // Сброс урона
        }
    }
}









