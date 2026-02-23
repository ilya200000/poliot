package name.modid;

import net.minecraft.network.packet.Packet;
import java.util.concurrent.LinkedBlockingQueue;

public class ElytraData {
    public static final LinkedBlockingQueue<Packet<?>> packetQueue = new LinkedBlockingQueue<>();
    public static int lastTeleportId = -1;
    public static boolean isFlying = false;
}



