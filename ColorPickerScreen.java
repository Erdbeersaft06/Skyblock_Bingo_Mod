package dev.skyblockbingo;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Farbwaehler: Farbfeld (Saettigung und Helligkeit) plus Farbleiste (Farbton). Aenderungen gelten sofort. */
public final class ColorPickerScreen extends Screen {
	private static final int SIZE = 128;
	private static final int BAR_W = 16;
	private static final int WHITE = 0xFFFFFFFF;

	private final Screen parent;
	private final String key;
	private float hue;
	private float sat;
	private float val;
	private boolean dragField = false;
	private boolean dragBar = false;
	private int px;
	private int py;
	private int hx;

	public ColorPickerScreen(Screen parent, String key) {
		super(Component.literal(Lang.t("picker.title", ConfigScreen.colorLabel(key))));
		this.parent = parent;
		this.key = key;
		int rgb = Palette.of(Bingo.cfg).colorOf(key) & 0xFFFFFF;
		float[] hsv = toHsv(rgb);
		this.hue = hsv[0];
		this.sat = hsv[1];
		this.val = hsv[2];
	}

	// ---------------------------------------------------------------- Farbrechnung

	/** HSV (je 0..1) nach ARGB. */
	static int hsv(float h, float s, float v) {
		float hh = (h - (float) Math.floor(h)) * 6f;
		int i = (int) hh;
		float f = hh - i;
		float p = v * (1 - s);
		float q = v * (1 - s * f);
		float t = v * (1 - s * (1 - f));
		float r;
		float g;
		float b;
		switch (i % 6) {
			case 0 -> { r = v; g = t; b = p; }
			case 1 -> { r = q; g = v; b = p; }
			case 2 -> { r = p; g = v; b = t; }
			case 3 -> { r = p; g = q; b = v; }
			case 4 -> { r = t; g = p; b = v; }
			default -> { r = v; g = p; b = q; }
		}
		return 0xFF000000 | (Math.round(r * 255) << 16) | (Math.round(g * 255) << 8) | Math.round(b * 255);
	}

	static float[] toHsv(int rgb) {
		float r = ((rgb >> 16) & 0xFF) / 255f;
		float g = ((rgb >> 8) & 0xFF) / 255f;
		float b = (rgb & 0xFF) / 255f;
		float max = Math.max(r, Math.max(g, b));
		float min = Math.min(r, Math.min(g, b));
		float d = max - min;
		float h;
		if (d == 0) {
			h = 0;
		} else if (max == r) {
			h = ((g - b) / d) % 6f;
		} else if (max == g) {
			h = (b - r) / d + 2f;
		} else {
			h = (r - g) / d + 4f;
		}
		h /= 6f;
		if (h < 0) h += 1f;
		return new float[] {h, max == 0 ? 0 : d / max, max};
	}

	private static float clamp01(double x) {
		return (float) Math.max(0, Math.min(1, x));
	}

	private void apply() {
		Config cfg = Bingo.cfg;
		Palette.ensureCustom(cfg);
		cfg.custom.put(key, String.format("%06X", hsv(hue, sat, val) & 0xFFFFFF));
		cfg.theme = "custom";
	}

	// ---------------------------------------------------------------- Aufbau und Anzeige

	@Override
	protected void init() {
		px = (this.width - (SIZE + 12 + BAR_W)) / 2;
		py = Math.max(30, (this.height - (SIZE + 90)) / 2 + 14);
		hx = px + SIZE + 12;
		this.addRenderableWidget(Button.builder(Component.literal(Lang.t("mover.done")), b -> this.onClose())
			.bounds((this.width - 120) / 2, py + SIZE + 38, 120, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		super.extractRenderState(g, mouseX, mouseY, delta);

		String title = Lang.t("picker.title", ConfigScreen.colorLabel(key));
		g.text(this.font, title, (this.width - this.font.width(title)) / 2, py - 16, WHITE, true);

		// Farbfeld: von links (weiss) nach rechts (reiner Farbton), nach unten dunkler
		for (int i = 0; i < SIZE; i++) {
			float s = i / (float) (SIZE - 1);
			g.fillGradient(px + i, py, px + i + 1, py + SIZE, hsv(hue, s, 1f), 0xFF000000);
		}
		g.outline(px - 1, py - 1, SIZE + 2, SIZE + 2, WHITE);

		// Farbleiste (Farbton)
		for (int j = 0; j < SIZE; j++) {
			g.fill(hx, py + j, hx + BAR_W, py + j + 1, hsv(j / (float) (SIZE - 1), 1f, 1f));
		}
		g.outline(hx - 1, py - 1, BAR_W + 2, SIZE + 2, WHITE);

		// Markierungen
		int mx = px + Math.round(sat * (SIZE - 1));
		int my = py + Math.round((1f - val) * (SIZE - 1));
		g.outline(mx - 3, my - 3, 7, 7, WHITE);
		g.outline(mx - 2, my - 2, 5, 5, 0xFF000000);
		int hy = py + Math.round(hue * (SIZE - 1));
		g.outline(hx - 2, hy - 1, BAR_W + 4, 3, WHITE);

		// Vorschau und Hex-Wert
		int cur = hsv(hue, sat, val);
		g.fill(px, py + SIZE + 8, px + 20, py + SIZE + 24, cur);
		g.outline(px - 1, py + SIZE + 7, 22, 18, WHITE);
		g.text(this.font, String.format("#%06X", cur & 0xFFFFFF), px + 28, py + SIZE + 12, WHITE, true);

		String hint = Lang.t("picker.hint");
		g.text(this.font, hint, (this.width - this.font.width(hint)) / 2, py + SIZE + 64, 0xFFBBBBBB, true);
	}

	// ---------------------------------------------------------------- Maus

	private boolean inField(double x, double y) {
		return x >= px && x < px + SIZE && y >= py && y < py + SIZE;
	}

	private boolean inBar(double x, double y) {
		return x >= hx && x < hx + BAR_W && y >= py && y < py + SIZE;
	}

	private void setField(double x, double y) {
		sat = clamp01((x - px) / (SIZE - 1));
		val = 1f - clamp01((y - py) / (SIZE - 1));
		apply();
	}

	private void setBar(double y) {
		hue = clamp01((y - py) / (SIZE - 1));
		apply();
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (super.mouseClicked(event, doubleClick)) return true;
		double x = event.x();
		double y = event.y();
		if (inField(x, y)) {
			dragField = true;
			setField(x, y);
			return true;
		}
		if (inBar(x, y)) {
			dragBar = true;
			setBar(y);
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (dragField) {
			setField(event.x(), event.y());
			return true;
		}
		if (dragBar) {
			setBar(event.y());
			return true;
		}
		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (dragField || dragBar) {
			dragField = false;
			dragBar = false;
			Bingo.cfg.save();
			return true;
		}
		return super.mouseReleased(event);
	}

	@Override
	public void onClose() {
		Bingo.cfg.save();
		this.minecraft.gui.setScreen(parent);
	}
}
