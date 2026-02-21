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

        // Самая жесткая защита от крашей (проверка всего)
        if (player == null || player.networkHandler == null) return;

        if (player.isFallFlying()) {
            MinecraftClient client = MinecraftClient.getInstance();
            Vec3d look = player.getRotationVec(1.0F);
            
            // СКОРОСТЬ ДЛЯ REALLYWORLD: 0.13 — это край. 
            // Если будет флажить (откидывать), снизь до 0.1.
            double speed = 0.8;

            // 1. ДВИЖЕНИЕ (Только если жмешь W)
            if (client.options.forwardKey.isPressed()) {
                // Вместо addVelocity, ставим жесткий вектор, но МЯГКИЙ
                Vec3d currentVel = player.getVelocity();
                player.setVelocity(look.x * speed, currentVel.y, look.z * speed);
            }

            // 2. BYPASS GRIM (Вертикальный обход)
            // Самое важное: сервер RW кикает, если ты не падаешь.
            // Мы делаем "ступеньку": 3 тика висим, 1 тик падаем.
            if (player.age % 4 == 0) {
                player.addVelocity(0, -0.05, 0); // Имитация падения
            } else {
                // Удерживаем высоту микро-импульсом
                Vec3d v = player.getVelocity();
                player.setVelocity(v.x, 0.01, v.z);
            }

            // 3. NO-FALL (Чтобы не сдохнуть при лаге)
            player.onLanding();
        }
    }
}










