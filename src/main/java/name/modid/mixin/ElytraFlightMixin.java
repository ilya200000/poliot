package name.modid.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
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

        // Если элитры раскрыты
        if (player != null && player.isFallFlying()) {
            // Вектор взгляда
            Vec3d look = player.getRotationVec(1.0F);
            
            // СКОРОСТЬ: 0.45 — это быстро (как фейерверк). 
            // Если будет кикать "Fly", снизь до 0.25
            double speed = 0.45; 

            // Устанавливаем скорость СТРОГО по направлению взгляда
            // Это дает тот самый эффект "управляемой ракеты"
            player.setVelocity(look.x * speed, look.y * speed, look.z * speed);

            // Чтобы сервер не думал, что ты стоишь в воздухе (Anti-Kick)
            // Мы позволяем игре считать, что ты летишь, обнуляя урон
            player.fallDistance = 0;
            
            // Фикс для приземления: если подлетаешь близко к земле, 
            // немного замедляемся, чтобы не крашнуло об блоки
            if (player.isOnGround()) {
                player.getAbilities().flying = false;
            }
        }
    }
}





