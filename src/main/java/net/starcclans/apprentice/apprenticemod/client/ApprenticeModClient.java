package net.starcclans.apprentice.apprenticemod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.starcclans.apprentice.apprenticemod.screen.CustomVillagerScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ApprenticeModClient implements ClientModInitializer {
    private static final Logger LOGGER = LogManager.getLogger("apprentice-mod-client");

    @Override
    public void onInitializeClient() {
        // Register your client-side networking handlers here
        ClientPlayNetworking.registerGlobalReceiver(new Identifier("apprentice-mod", "open_custom_ui"), (client, handler, buf, responseSender) -> {
            LOGGER.info("Received custom UI packet.");
            if (client.world != null) {
                MinecraftClient.getInstance().execute(() -> {
                    MinecraftClient.getInstance().setScreen(new CustomVillagerScreen(Text.of("Custom Villager UI")));
                });
            }
        });
    }
}
