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

public class ApprenticeMod implements ModInitializer {
    @Override
    public void onInitialize() {
        // Register your event listeners and other initialization code here

        // Example: Registering a UseEntityCallback for villager interaction
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient && entity instanceof VillagerEntity) {
                // Custom villager interaction logic here
                ServerPlayNetworking.send((ServerPlayerEntity) player, new Identifier("apprentice-mod", "open_custom_ui"), new PacketByteBuf(Unpooled.buffer()));
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
    }
}
