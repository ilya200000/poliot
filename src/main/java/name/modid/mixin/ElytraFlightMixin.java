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

        // Самая простая проверка полета
        if (player.isFallFlying()) {
            // Получаем вектор взгляда (этот метод стабилен)
            Vec3d look = player.getRotationVec(1.0F);
            
            // Даем постоянный небольшой импульс вперед (авто-полет)
            // 0.15 - медленно, но очень стабильно и не крашит
            double s = 0.15;
            player.addVelocity(look.x * s, look.y * s, look.z * s);

            // Фикс гравитации: летим ровно, не падаем
            Vec3d v = player.getVelocity();
            player.setVelocity(v.x, v.y + 0.05, v.z);
            
            // Сброс урона
            player.onLanding();
        }
    }
}



