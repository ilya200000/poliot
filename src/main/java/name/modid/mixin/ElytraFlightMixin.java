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

        // Полная защита от вылетов при загрузке
        if (player == null || player.networkHandler == null) return;

        if (player.isFallFlying()) {
            MinecraftClient client = MinecraftClient.getInstance();
            
            // Читаем кнопку напрямую (без опасных полей)
            if (client.options.forwardKey.isPressed()) {
                Vec3d look = player.getRotationVec(1.0F);
                
                // СКОРОСТЬ: 0.12 - лимит для MysteryWorld / BedWars
                double speed = 0.12;

                // Вместо пакетов используем addVelocity. 
                // В 1.21.11 это самый стабильный способ.
                player.addVelocity(look.x * speed, look.y * speed, look.z * speed);
                
                // ANTI-KICK: Небольшая коррекция высоты, чтобы сервер не видел Fly
                Vec3d v = player.getVelocity();
                player.setVelocity(v.x, v.y + 0.05, v.z);
            } else {
                // Если W не нажата - просто парим без падения
                Vec3d v = player.getVelocity();
                player.setVelocity(v.x, -0.005, v.z);
            }

            // Убираем урон от падения
            player.onLanding();
        }
    }
}








