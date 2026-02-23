package name.modid.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
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

        // 1. АВТОВЗЛЕТ
        if (!player.isOnGround() && player.getVelocity().y < -0.1 && !player.isFallFlying()) {
            player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }

        if (player.isFallFlying()) {
            // 2. БАЙПАС "PITCH-HOP"
            // Мы не трогаем скорость! Мы заставляем сервер думать, что ты постоянно 
            // "перезапускаешь" полет. Это убирает трение воздуха в 1.20.1.
            if (player.input.pressingForward) {
                if (player.age % 10 == 0) {
                    // Переоткрываем элитру без закрытия (серверный глитч)
                    player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                }

                // 3. ЛЕГИТНОЕ ПЛАНИРОВАНИЕ
                // Мы просто ставим взгляд чуть-чуть вверх (-2.0), чтобы не падать быстро.
                // Это не чит, это "зажатый макрос" на угол взгляда.
                if (player.getPitch() > -2.0F && !player.input.jumping) {
                    player.setPitch(-2.0F);
                }
            }
            
            // Если зажат прыжок — мод плавно тянет нос вверх для набора высоты
            if (player.input.jumping) {
                player.setPitch(-15.0F);
            }
        }
    }
}


























