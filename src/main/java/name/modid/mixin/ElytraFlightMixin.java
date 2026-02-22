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
        if (player == null || !player.isFallFlying()) return;

        // На MysteryWorld нельзя лететь вверх слишком быстро — кикнет за "Fly"
        // Поэтому мы будем просто поддерживать горизонтальную скорость.
        
        Vec3d look = player.getRotationVec(1.0F);
        Vec3d velocity = player.getVelocity();

        // 1. МЯГКОЕ УСКОРЕНИЕ (Bypass для Grim)
        // Мы не ставим скорость (setVelocity), а добавляем её (addVelocity)
        // 0.05 - это безопасный порог, при котором античит думает, что ты просто хорошо планируешь
        double speedMultiplier = 0.05;

        if (player.input.jumping) {
            // Если зажат пробел — медленно набираем высоту (имитация фейерверка)
            player.addVelocity(0, 0.03, 0);
        }

        // Подталкиваем игрока вперед по направлению взгляда
        player.addVelocity(
            look.x * speedMultiplier,
            0, // Высоту лучше не трогать напрямую, чтобы не флагало
            look.z * speedMultiplier
        );

        // 2. ЛИМИТЕР (Чтобы тебя не разогнало до бана)
        Vec3d v = player.getVelocity();
        double maxSpeed = 0.6; // Предел скорости для Mystery
        if (v.horizontalLength() > maxSpeed) {
            player.setVelocity(v.x * 0.9, v.y, v.z * 0.9);
        }

        // 3. АНТИ-ГРАВИТАЦИЯ (Чтобы не падать камнем вниз)
        // Каждые 2 тика замедляем падение
        if (player.age % 2 == 0 && v.y < 0) {
            player.setVelocity(v.x, v.y * 0.8, v.z);
        }
    }
}










