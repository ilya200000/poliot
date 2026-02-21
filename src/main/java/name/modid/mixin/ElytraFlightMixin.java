package name.modid.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class ElytraFlightMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if ((Object) this instanceof PlayerEntity player) {
            if (player.isFallFlying()) {
                // Логика полета: убираем падение и добавляем скорость вперед
                player.getAbilities().flying = true;
                player.getAbilities().setFlySpeed(0.05f);
            }
        }
    }
}
