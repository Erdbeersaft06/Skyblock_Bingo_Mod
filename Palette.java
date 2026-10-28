package dev.skyblockbingo;

import java.util.List;

/** Farben fuers HUD (ARGB). */
public record Palette(int bg, int cell, int text, int muted, int p0, int p1, int border) {
	public static final List<String> KEYS = List.of("bg", "cell", "text", "muted", "p0", "p1", "border");

	private static final Palette DARK = new Palette(
		0xE0151B2F, 0xFF1E2742, 0xFFE5E7EB, 0xFF94A3B8, 0xFF3B82F6, 0xFFF97316, 0xFF3A4468);
	private static final Palette LIGHT = new Palette(
		0xE6FFFFFF, 0xFFE8EDF5, 0xFF0F172A, 0xFF64748B, 0xFF2563EB, 0xFFEA580C, 0xFFB0B8C8);

	public static Palette of(Config c) {
		return switch (c.theme) {
			case "light" -> LIGHT;
			case "custom" -> custom(c);
			default -> DARK;
		};
	}

	/** Eine Farbe der aktuellen Standard-Palette als RRGGBB (Startwert fuer "custom"). */
	public static String hexOf(Palette p, String key) {
		int v = switch (key) {
			case "bg" -> p.bg;
			case "cell" -> p.cell;
			case "text" -> p.text;
			case "muted" -> p.muted;
			case "p0" -> p.p0;
			case "p1" -> p.p1;
			default -> p.border;
		};
		return String.format("%06X", v & 0xFFFFFF);
	}

	private static Palette custom(Config c) {
		Palette d = DARK;
		return new Palette(
			(0xE0 << 24) | parse(c, "bg", d.bg),
			0xFF000000 | parse(c, "cell", d.cell),
			0xFF000000 | parse(c, "text", d.text),
			0xFF000000 | parse(c, "muted", d.muted),
			0xFF000000 | parse(c, "p0", d.p0),
			0xFF000000 | parse(c, "p1", d.p1),
			0xFF000000 | parse(c, "border", d.border));
	}

	private static int parse(Config c, String key, int fallback) {
		String s = c.custom.get(key);
		if (s != null) {
			try {
				return Integer.parseInt(s, 16) & 0xFFFFFF;
			} catch (NumberFormatException ignored) {
				// ungueltig -> Standardfarbe
			}
		}
		return fallback & 0xFFFFFF;
	}
}
