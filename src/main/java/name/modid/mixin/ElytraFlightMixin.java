@Mixin(ClientPlayerEntity.class)
public abstract class ElytraFlightMixin {
    
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        if (player == null || player.networkHandler == null) return;

        // 1. Умный автовзлет (без рывков)
        if (!player.isOnGround() && player.getVelocity().y < -0.01 && !player.isFallFlying()) {
            // Вместо checkFallFlying отправляем пакет о начале полета
            player.networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }

        if (player.isFallFlying()) {
            Vec3d look = player.getRotationVec(1.0F);
            
            // Безопасная скорость для 1.20.1
            double speedMultiplier = 0.07; 

            // 2. Движение вперед (W)
            if (player.input.pressingForward) {
                Vec3d velocity = player.getVelocity();
                // Используем плавное приращение, чтобы античит не видел телепортации
                player.setVelocity(velocity.add(look.x * speedMultiplier, look.y * speedMultiplier, look.z * speedMultiplier));
            }

            // 3. Управление высотой
            if (player.input.jumping) {
                player.addVelocity(0, 0.04, 0); // Плавный подъем
            } else if (player.input.sneaking) {
                player.addVelocity(0, -0.2, 0); // Быстрый спуск
            } else {
                // ВАЖНО: Не ставим Y в 0.0. Оставляем минимальное падение (-0.01), 
                // чтобы сервер не кикнул за "Hover" (левитацию).
                Vec3d v = player.getVelocity();
                if (v.y < -0.01) {
                    player.setVelocity(v.x, -0.01, v.z);
                }
            }
            
            // Убираем player.onLanding(), на 1.20.1 это вызывает десинк.
        }
    }
}









