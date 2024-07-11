package net.starcclans.apprentice.apprenticemod;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ApprenticeMod implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("apprentice-mod");
    public static final Identifier OPEN_CUSTOM_UI_PACKET_ID = new Identifier("apprentice-mod", "open_custom_ui");

    @Override
    public void onInitialize() {
        // Register your event listeners and other initialization code here

        // Example: Registering a UseEntityCallback for villager interaction
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient && entity instanceof VillagerEntity villager) {
                LOGGER.info("Villager interaction detected, sending custom UI packet.");

                // Make the villager face the player
                        double deltaX = player.getX() - villager.getX();
                double deltaZ = player.getZ() - villager.getZ();
                float yaw = (float)(Math.atan2(deltaZ, deltaX) * (180 / Math.PI)) - 90;
                villager.setYaw(yaw);
                villager.headYaw = yaw;

                // Send the packet to open the custom UI
                ServerPlayNetworking.send((ServerPlayerEntity) player, OPEN_CUSTOM_UI_PACKET_ID, new PacketByteBuf(Unpooled.buffer()));
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
    }
}
