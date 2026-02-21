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

        // Полная защита от краша при заходе
        if (player == null || player.networkHandler == null) return;

        if (player.isFallFlying()) {
            MinecraftClient client = MinecraftClient.getInstance();
            
            // Проверка кнопки через настройки (самый стабильный способ)
            if (client.options.forwardKey.isPressed()) {
                Vec3d look = player.getRotationVec(1.0F);
                
                // СКОРОСТЬ ДЛЯ REALLYWORLD: 0.11 - 0.13. 
                // Это "тихий" режим, который античит не видит как Fly.
                double speed = 0.12;

                // Вместо создания пакетов (которые крашат), используем addVelocity. 
                // В 1.21.11 это единственный 100% стабильный способ.
                player.addVelocity(look.x * speed, look.y * speed, look.z * speed);
                
                // BYPASS ДЛЯ MYSTERY/REALLYWORLD (Anti-Fly):
                // Античит палит, если ты не падаешь. Мы заставляем персонажа 
                // микроскопически "дрожать" по вертикали, имитируя полет.
                Vec3d v = player.getVelocity();
                if (player.age % 4 == 0) {
                    player.setVelocity(v.x, v.y - 0.03, v.z); // Провал вниз
                } else {
                    player.setVelocity(v.x, v.y + 0.025, v.z); // Подтяжка вверх
                }
            } else {
                // Если W не нажата - просто парим без падения камнем
                Vec3d v = player.getVelocity();
                player.setVelocity(v.x, -0.005, v.z);
            }

            // Убираем урон от падения
            player.onLanding();
        }
    }
}










