package dev.skyblockbingo;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

import java.util.ArrayList;
import java.util.List;

/** Zeichnet Board, Timer und Spielstand ins HUD. */
public final class BingoHud {
	private static final int GOLD = 0xFFFACC15;
	private static final int DARK_TEXT = 0xFF1A1A1A;
	private static final int WHITE = 0xFFFFFFFF;

	private BingoHud() {}

	public static String formatTime(long ms) {
		long s = Math.max(0, ms / 1000);
		long h = s / 3600;
		long m = (s % 3600) / 60;
		long x = s % 60;
		return h > 0 ? String.format("%d:%02d:%02d", h, m, x) : String.format("%02d:%02d", m, x);
	}

	public static void extract(GuiGraphicsExtractor g, DeltaTracker tickCounter) {
		Config cfg = Bingo.cfg;
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		Bingo.pollMouse(mc);
		if (!cfg.hudVisible || Bingo.role == Bingo.Role.NONE) return;
		// Waehrend das Verschieben-Fenster offen ist, zeichnet es das Board selbst
		if (mc.gui.screen() instanceof MoverScreen || mc.gui.screen() instanceof BoardScreen) return;
		Font font = mc.font;
		Palette pal = Palette.of(cfg);

		Game game = Bingo.game;
		if (game == null) {
			drawLobby(g, font, pal, cfg.hudX, cfg.hudY);
			return;
		}
		drawBoard(g, mc, font, pal, game, cfg);

		// Im Inventar: Feld unter der Maus hervorheben
		if (cfg.inventoryClick && !game.over && mc.gui.screen() instanceof InventoryScreen) {
			double[] m = Bingo.mouseGui(mc);
			int i = cellAt(mc, font, game, cfg, m[0], m[1]);
			if (i >= 0) {
				int[] r = cellRect(mc, font, game, cfg, i);
				g.outline(r[0], r[1], r[2], r[2], GOLD);
				g.outline(r[0] + 1, r[1] + 1, r[2] - 2, r[2] - 2, GOLD);
			}
		}
	}

	/** Liefert {Feldgroesse, Breite, Hoehe} des Boards in GUI-Pixeln. */
	static int[] measure(Font font, int n, boolean over, Config cfg) {
		int gap = 2;
		int pad = 4;
		int lh = font.lineHeight + 1;
		int headerH = lh * 2 + 3;
		int bannerH = over ? lh + 7 : 0;
		int cell = Math.max(20, cfg.cell);
		int bw = n * cell + (n - 1) * gap;
		return new int[] {cell, bw + pad * 2, pad + headerH + bw + pad + bannerH};
	}

	/** Rechteck eines Feldes: {x, y, Seitenlaenge}. */
	static int[] cellRect(Minecraft mc, Font font, Game game, Config cfg, int i) {
		int n = game.size;
		int gap = 2;
		int pad = 4;
		int lh = font.lineHeight + 1;
		int headerH = lh * 2 + 3;
		int[] ms = measure(font, n, game.over, cfg);
		int[] pos = position(mc, ms[1], ms[2], cfg);
		int cx = pos[0] + pad + (i % n) * (ms[0] + gap);
		int cy = pos[1] + pad + headerH + (i / n) * (ms[0] + gap);
		return new int[] {cx, cy, ms[0]};
	}

	/** Index des Feldes unter dem Mauszeiger oder -1. */
	static int cellAt(Minecraft mc, Font font, Game game, Config cfg, double mx, double my) {
		int n = game.size;
		int gap = 2;
		int pad = 4;
		int lh = font.lineHeight + 1;
		int headerH = lh * 2 + 3;
		int[] ms = measure(font, n, game.over, cfg);
		int[] pos = position(mc, ms[1], ms[2], cfg);
		int gx = pos[0] + pad;
		int gy = pos[1] + pad + headerH;
		for (int i = 0; i < n * n; i++) {
			int cx = gx + (i % n) * (ms[0] + gap);
			int cy = gy + (i / n) * (ms[0] + gap);
			if (mx >= cx && mx < cx + ms[0] && my >= cy && my < cy + ms[0]) return i;
		}
		return -1;
	}

	/** Position des Boards, so begrenzt dass es immer ganz im Fenster bleibt. */
	static int[] position(Minecraft mc, int pw, int ph, Config cfg) {
		int gw = mc.getWindow().getGuiScaledWidth();
		int gh = mc.getWindow().getGuiScaledHeight();
		int x = Math.max(0, Math.min(cfg.hudX, gw - pw));
		int y = Math.max(0, Math.min(cfg.hudY, gh - ph));
		return new int[] {x, y};
	}

	private static void drawLobby(GuiGraphicsExtractor g, Font font, Palette pal, int x, int y) {
		List<String> lines = new ArrayList<>();
		switch (Bingo.role) {
			case HOST -> {
				lines.add(Lang.t("hud.code", Bingo.code));
				lines.add(Bingo.connected ? Lang.t("hud.peer_connected", Bingo.names[1]) : Lang.t("hud.waiting_peer"));
				lines.add(Bingo.connected ? Lang.t("hud.start_hint") : Lang.t("hud.join_hint", Bingo.code));
			}
			case GUEST -> {
				lines.add(Lang.t("hud.code", Bingo.code));
				lines.add(Bingo.connected ? Lang.t("hud.connected_to", Bingo.names[0]) : Lang.t("hud.connecting"));
				if (Bingo.connected) lines.add(Lang.t("hud.wait_start"));
			}
			case SOLO -> {
				lines.add(Lang.t("hud.solo"));
				lines.add(Lang.t("hud.start_hint"));
			}
			default -> {
				return;
			}
		}
		int lh = font.lineHeight + 2;
		int w = 0;
		for (String s : lines) w = Math.max(w, font.width(s));
		w += 12;
		int h = lines.size() * lh + 8;
		g.fill(x, y, x + w, y + h, pal.bg());
		g.outline(x, y, w, h, pal.border());
		int ty = y + 5;
		for (int i = 0; i < lines.size(); i++) {
			g.text(font, lines.get(i), x + 6, ty, i == 0 ? pal.text() : pal.muted(), false);
			ty += lh;
		}
	}

	static void drawBoard(GuiGraphicsExtractor g, Minecraft mc, Font font, Palette pal, Game game, Config cfg) {
		final int n = game.size;
		final int gap = 2;
		final int pad = 4;
		final int lh = font.lineHeight + 1;

		int headerH = lh * 2 + 3;
		int bannerH = game.over ? lh + 7 : 0;
		int[] ms = measure(font, n, game.over, cfg);
		int cell = ms[0];
		int pw = ms[1];
		int ph = ms[2];
		int bw = n * cell + (n - 1) * gap;
		int[] pos = position(mc, pw, ph, cfg);
		final int x = pos[0];
		final int y = pos[1];

		g.fill(x, y, x + pw, y + ph, pal.bg());
		g.outline(x, y, pw, ph, pal.border());

		// Kopfzeile: Zeit, Modus
		String top = Lang.t("hud.time", formatTime(game.elapsed()), n, n, game.lockout ? " Lockout" : "");
		g.text(font, top, x + pad + 1, y + pad, pal.text(), false);

		// Spieler und Punktestand
		int px = x + pad + 1;
		int py = y + pad + lh + 1;
		for (int p = 0; p < game.np; p++) {
			int col = p == 0 ? pal.p0() : pal.p1();
			g.fill(px, py + 1, px + 6, py + 7, col);
			String label = Bingo.names[p] + ": " + game.count(p);
			g.text(font, label, px + 9, py, pal.text(), false);
			px += 9 + font.width(label) + 10;
		}

		// Felder
		int gx = x + pad;
		int gy = y + pad + headerH;
		for (int i = 0; i < game.board.length; i++) {
			int r = i / n;
			int c = i % n;
			int cx = gx + c * (cell + gap);
			int cy = gy + r * (cell + gap);
			boolean m0 = game.marks[i][0];
			boolean m1 = game.marks[i][1];
			boolean marked = m0 || m1;

			if (m0 && m1) {
				int half = cell / 2;
				g.fill(cx, cy, cx + half, cy + cell, pal.p0());
				g.fill(cx + half, cy, cx + cell, cy + cell, pal.p1());
			} else if (m0) {
				g.fill(cx, cy, cx + cell, cy + cell, pal.p0());
			} else if (m1) {
				g.fill(cx, cy, cx + cell, cy + cell, pal.p1());
			} else {
				g.fill(cx, cy, cx + cell, cy + cell, pal.cell());
				g.outline(cx, cy, cell, cell, pal.border());
			}

			if (game.over && game.inWinLine(i)) {
				g.outline(cx, cy, cell, cell, GOLD);
				g.outline(cx + 1, cy + 1, cell - 2, cell - 2, GOLD);
			}

			int textColor = marked ? WHITE : pal.text();
			int numColor = marked ? 0xCCFFFFFF : pal.muted();
			g.text(font, String.valueOf(i + 1), cx + 2, cy + 2, numColor, marked);
			int maxLines = Math.max(1, (cell - 4) / lh - 1);
			List<String> lines = wrap(font, game.board[i], cell - 4, maxLines);
			int ty = cy + 2 + lh;
			for (String s : lines) {
				g.text(font, s, cx + 2, ty, textColor, marked);
				ty += lh;
			}
		}

		// Sieg-Banner
		if (game.over) {
			int by = gy + bw + 3;
			g.fill(x + pad, by, x + pw - pad, by + lh + 3, GOLD);
			String msg = game.np == 1
				? Lang.t("hud.bingo", formatTime(game.finalMs))
				: Lang.t("hud.wins", Bingo.names[Math.max(0, game.winner)], formatTime(game.finalMs));
			g.text(font, msg, x + pad + 4, by + 2, DARK_TEXT, false);
		}
	}

	static List<String> wrap(Font font, String s, int maxW, int maxLines) {
		List<String> out = new ArrayList<>();
		StringBuilder cur = new StringBuilder();
		for (String word : s.split(" ")) {
			if (word.isEmpty()) continue;
			while (font.width(word) > maxW && word.length() > 1) {
				int k = word.length() - 1;
				while (k > 1 && font.width(word.substring(0, k)) > maxW) k--;
				if (cur.length() > 0) {
					out.add(cur.toString());
					cur.setLength(0);
				}
				out.add(word.substring(0, k));
				word = word.substring(k);
			}
			String trial = cur.length() == 0 ? word : cur + " " + word;
			if (font.width(trial) <= maxW) {
				cur.setLength(0);
				cur.append(trial);
			} else {
				out.add(cur.toString());
				cur.setLength(0);
				cur.append(word);
			}
		}
		if (cur.length() > 0) out.add(cur.toString());
		if (out.size() > maxLines) {
			List<String> cut = new ArrayList<>(out.subList(0, maxLines));
			String last = cut.get(maxLines - 1);
			while (last.length() > 1 && font.width(last + "..") > maxW) last = last.substring(0, last.length() - 1);
			cut.set(maxLines - 1, last + "..");
			return cut;
		}
		return out;
	}
}
