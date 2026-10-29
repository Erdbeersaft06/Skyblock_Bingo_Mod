package dev.skyblockbingo;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Zeigt das echte Board; ein Mausklick auf ein Feld hakt es ab (oder nimmt die Markierung zurueck). */
public final class BoardScreen extends Screen {
	public BoardScreen() {
		super(Component.literal("Skyblock Bingo - Board"));
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);

		Game game = Bingo.game;
		String hint;
		if (game == null) {
			hint = "Keine Runde aktiv. Im Menü (/sbb) unter \"Spiel\" eine Runde starten.";
		} else {
			BingoHud.drawBoard(graphics, this.minecraft, this.font, Palette.of(Bingo.cfg), game, Bingo.cfg);
			int hover = BingoHud.cellAt(this.minecraft, this.font, game, Bingo.cfg, mouseX, mouseY);
			if (hover >= 0 && !game.over) {
				hint = "Klick: Feld " + (hover + 1) + " abhaken / zurücknehmen";
			} else {
				hint = game.over ? "Runde vorbei. Neue Runde im Menü unter \"Spiel\"." : "Klick auf ein Feld hakt es ab.";
			}
		}
		graphics.text(this.font, hint, (this.width - this.font.width(hint)) / 2, this.height - 20, 0xFFFFFFFF, true);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (super.mouseClicked(event, doubleClick)) return true;
		Game game = Bingo.game;
		if (game == null || game.over) return false;
		int i = BingoHud.cellAt(this.minecraft, this.font, game, Bingo.cfg, event.x(), event.y());
		if (i < 0) return false;
		String err = Bingo.mark(i);
		if (err != null) Bingo.chat(err);
		return true;
	}

	@Override
	public void onClose() {
		this.minecraft.gui.setScreen(null);
	}
}
