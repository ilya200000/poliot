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

        // Если элитры надеты, но мы еще не летим - спамим пакет взлета
        if (!player.isOnGround() && !player.isFallFlying() && player.getVelocity().y < -0.08) {
            player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }

        if (player.isFallFlying()) {
            Vec3d look = player.getRotationVec(1.0F);
            
            // Снижаем скорость до 0.18 - это предел для многих античитов (Grim/Matrix)
            double s = 0.18; 
            
            // Постоянный импульс вперед
            player.addVelocity(look.x * s, look.y * s, look.z * s);

            // ANTI-KICK: Серверу нужно видеть, что ты падаешь.
            // Каждые 5 тиков мы принудительно тянем тебя вниз на мизерное расстояние.
            if (player.age % 5 == 0) {
                player.addVelocity(0, -0.02, 0);
            } else {
                // В остальное время держим высоту
                player.addVelocity(0, 0.015, 0);
            }
            
            player.fallDistance = 0;
        }
    }
}




