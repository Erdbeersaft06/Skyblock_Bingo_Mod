package dev.skyblockbingo;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;

public final class BingoMod implements ClientModInitializer {
	public static final String MOD_ID = "skyblockbingo";

	@Override
	public void onInitializeClient() {
		Bingo.cfg = Config.load();
		BingoCommands.init();

		HudElementRegistry.attachElementBefore(
			VanillaHudElements.CHAT,
			Identifier.fromNamespaceAndPath(MOD_ID, "board"),
			BingoHud::extract);

		ClientTickEvents.END_CLIENT_TICK.register(client -> Bingo.tick());
	}
}
