package name.modid.mixin;

import net.minecraft.client.MinecraftClient;
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

        if (player != null && player.isFallFlying()) {
            // Прямая проверка кнопок через MinecraftClient (самый надежный способ)
            MinecraftClient client = MinecraftClient.getInstance();
            boolean isForward = client.options.forwardKey.isPressed();
            boolean isJump = client.options.jumpKey.isPressed();

            Vec3d look = player.getRotationVec(1.0F);
            double speed = 0.25;

            if (isForward) {
                player.addVelocity(look.x * speed, look.y * speed, look.z * speed);
            } else {
                Vec3d v = player.getVelocity();
                player.setVelocity(v.x, -0.005, v.z);
            }

            if (isJump) {
                player.addVelocity(0, 0.1, 0);
            }

            player.setFallDistance(0);
        }
    }
}






