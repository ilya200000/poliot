package name.modid.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ElytraFlightMixin {

    private int boostTimer = 0;

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        if (player == null || player.networkHandler == null) return;

        // 1. АВТОВЗЛЕТ (Стандарт)
        if (!player.isOnGround() && player.getVelocity().y < -0.1 && !player.isFallFlying()) {
            player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }

        if (player.isFallFlying()) {
            // 2. БАЙПАС "FIREWORK SPOOF"
            // Когда жмешь W, мы шлем пакет, что ты заюзал предмет в руке. 
            // Сервер думает, что это фейерверк, и дает тебе ванильное ускорение.
            if (player.input.pressingForward) {
                boostTimer++;
                if (boostTimer >= 15) { // Не спамим, имитируем задержку фейерверка
                    // Шлем пакет "Использование предмета"
                    player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0));
                    boostTimer = 0;
                }
                
                // Минимальный "пинок" для клиента, чтобы синхронизировать движение
                player.addVelocity(player.getRotationVec(1.0F).x * 0.05, 0.01, player.getRotationVec(1.0F).z * 0.05);
            }

            // 3. УПРАВЛЕНИЕ ВЫСОТОЙ (Легит)
            if (player.input.jumping) {
                player.addVelocity(0, 0.02, 0);
            } else if (player.input.sneaking) {
                player.addVelocity(0, -0.2, 0);
            }
            
            // Если слишком медленно падаем - античит кикнет. 
            // Даем игроку падать со скоростью -0.05 (почти незаметно)
            if (player.getVelocity().y > -0.05 && !player.input.jumping) {
                player.setVelocity(player.getVelocity().x, -0.05, player.getVelocity().z);
            }
        }
    }
}












