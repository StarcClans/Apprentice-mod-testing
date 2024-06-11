package net.starcclans.apprentice.apprenticemod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.starcclans.apprentice.apprenticemod.screen.CustomVillagerScreen;

public class ApprenticeModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register your client-side networking handlers here
        ClientPlayNetworking.registerGlobalReceiver(new Identifier("apprentice-mod", "open_custom_ui"), (client, handler, buf, responseSender) -> client.execute(() -> {
            if (client.world != null) {
                client.world.getEntitiesByClass(VillagerEntity.class, client.world.getWorldBorder().asVoxelShape().getBoundingBox(), entity -> true)
                        .stream()
                        .findFirst().ifPresent(villager -> MinecraftClient.getInstance().setScreen(new CustomVillagerScreen(Text.of("Custom Villager UI"))));
            }
        }));
    }
}
