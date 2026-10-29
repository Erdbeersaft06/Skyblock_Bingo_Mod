package dev.skyblockbingo;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Fenster zum Verschieben (ziehen) und Skalieren (Mausrad) des Boards, aehnlich dem SkyHanni-GUI-Editor. */
public final class MoverScreen extends Screen {
	private static final int GOLD = 0xFFFACC15;
	private static final int WHITE = 0xFFFFFFFF;

	private final Game preview;
	private boolean dragging = false;
	private int grabX = 0;
	private int grabY = 0;
	private Button themeButton;

	public MoverScreen() {
		super(Component.literal("Skyblock Bingo - HUD verschieben"));
		this.preview = makePreview();
	}

	/** Vorschau-Board mit ein paar Beispiel-Markierungen, damit man die Farben sieht. */
	private static Game makePreview() {
		Config cfg = Bingo.cfg;
		int n = cfg.size;
		String[] board = new String[n * n];
		List<String> goals = cfg.goals;
		for (int i = 0; i < board.length; i++) {
			board[i] = i < goals.size() ? goals.get(i) : "Goal " + (i + 1);
		}
		Game g = new Game(n, cfg.lockout, 2, board, 0);
		g.marks[0][0] = true;
		g.marks[1][1] = true;
		if (board.length > 3) {
			g.marks[2][0] = true;
			g.marks[2][1] = !cfg.lockout;
			g.marks[3][0] = true;
		}
		return g;
	}

	@Override
	protected void init() {
		int w = 110;
		int h = 20;
		int gap = 6;
		int total = w * 3 + gap * 2;
		int sx = (this.width - total) / 2;
		int y = this.height - 28;

		themeButton = Button.builder(themeLabel(), b -> {
			cycleTheme();
			b.setMessage(themeLabel());
		}).bounds(sx, y, w, h).build();
		this.addRenderableWidget(themeButton);

		this.addRenderableWidget(Button.builder(Component.literal("Zurücksetzen"), b -> {
			Bingo.cfg.hudX = 6;
			Bingo.cfg.hudY = 6;
			Bingo.cfg.cell = 56;
			Bingo.cfg.save();
		}).bounds(sx + w + gap, y, w, h).build());

		this.addRenderableWidget(Button.builder(Component.literal("Fertig"), b -> this.onClose())
			.bounds(sx + (w + gap) * 2, y, w, h).build());
	}

	private static Component themeLabel() {
		String t = Bingo.cfg.theme;
		String name = t.equals("light") ? "Hell" : t.equals("custom") ? "Eigene" : "Dunkel";
		return Component.literal("Design: " + name);
	}

	private static void cycleTheme() {
		Config cfg = Bingo.cfg;
		String next = switch (cfg.theme) {
			case "dark" -> "light";
			case "light" -> "custom";
			default -> "dark";
		};
		if (next.equals("custom") && cfg.custom.isEmpty()) {
			Palette cur = Palette.of(cfg);
			for (String k : Palette.KEYS) cfg.custom.put(k, Palette.hexOf(cur, k));
		}
		cfg.theme = next;
		cfg.save();
	}

	private int[] measure() {
		return BingoHud.measure(this.font, preview.size, false, Bingo.cfg);
	}

	private int[] position(int[] m) {
		return BingoHud.position(this.minecraft, m[1], m[2], Bingo.cfg);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);

		Config cfg = Bingo.cfg;
		Palette pal = Palette.of(cfg);
		BingoHud.drawBoard(graphics, this.minecraft, this.font, pal, preview, cfg);

		int[] m = measure();
		int[] p = position(m);
		boolean hover = mouseX >= p[0] && mouseX < p[0] + m[1] && mouseY >= p[1] && mouseY < p[1] + m[2];
		if (hover || dragging) {
			graphics.outline(p[0] - 1, p[1] - 1, m[1] + 2, m[2] + 2, GOLD);
		}

		String l1 = "Ziehen: Board verschieben     Mausrad: Größe ändern";
		graphics.text(this.font, l1, (this.width - this.font.width(l1)) / 2, this.height - 52, WHITE, true);
		String l2 = "Position " + p[0] + ", " + p[1] + "     Feldgröße " + cfg.cell;
		graphics.text(this.font, l2, (this.width - this.font.width(l2)) / 2, this.height - 40, 0xFFCCCCCC, true);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (super.mouseClicked(event, doubleClick)) return true;
		int[] m = measure();
		int[] p = position(m);
		double mx = event.x();
		double my = event.y();
		if (mx >= p[0] && mx < p[0] + m[1] && my >= p[1] && my < p[1] + m[2]) {
			dragging = true;
			grabX = (int) mx - p[0];
			grabY = (int) my - p[1];
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (dragging) {
			int[] m = measure();
			int gw = this.minecraft.getWindow().getGuiScaledWidth();
			int gh = this.minecraft.getWindow().getGuiScaledHeight();
			Bingo.cfg.hudX = Math.max(0, Math.min((int) event.x() - grabX, gw - m[1]));
			Bingo.cfg.hudY = Math.max(0, Math.min((int) event.y() - grabY, gh - m[2]));
			return true;
		}
		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (dragging) {
			dragging = false;
			Bingo.cfg.save();
			return true;
		}
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (scrollY != 0) {
			Config cfg = Bingo.cfg;
			cfg.cell = Math.max(24, Math.min(100, cfg.cell + (scrollY > 0 ? 2 : -2)));
			int[] m = measure();
			int[] p = position(m);
			cfg.hudX = p[0];
			cfg.hudY = p[1];
			cfg.save();
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public void onClose() {
		Bingo.cfg.save();
		this.minecraft.gui.setScreen(null);
	}
}
