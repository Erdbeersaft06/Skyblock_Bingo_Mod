package dev.skyblockbingo;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

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
		if (!cfg.hudVisible || Bingo.role == Bingo.Role.NONE) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		Font font = mc.font;
		Palette pal = Palette.of(cfg);

		Game game = Bingo.game;
		if (game == null) {
			drawLobby(g, font, pal, cfg.hudX, cfg.hudY);
			return;
		}
		drawBoard(g, mc, font, pal, game, cfg);
	}

	private static void drawLobby(GuiGraphicsExtractor g, Font font, Palette pal, int x, int y) {
		List<String> lines = new ArrayList<>();
		switch (Bingo.role) {
			case HOST -> {
				lines.add("Party-Code: " + Bingo.code);
				lines.add(Bingo.connected ? Bingo.names[1] + " ist verbunden" : "Warte auf Mitspieler ...");
				lines.add(Bingo.connected ? "Starten mit /sbb start" : "Mitspieler: /sbb join " + Bingo.code);
			}
			case GUEST -> {
				lines.add("Party-Code: " + Bingo.code);
				lines.add(Bingo.connected ? "Verbunden mit " + Bingo.names[0] : "Verbinde ...");
				if (Bingo.connected) lines.add("Warte auf den Start");
			}
			case SOLO -> {
				lines.add("Solo-Modus");
				lines.add("Starten mit /sbb start");
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

	private static void drawBoard(GuiGraphicsExtractor g, Minecraft mc, Font font, Palette pal, Game game, Config cfg) {
		final int n = game.size;
		final int gap = 2;
		final int pad = 4;
		final int lh = font.lineHeight + 1;
		final int x = cfg.hudX;
		final int y = cfg.hudY;

		int headerH = lh * 2 + 3;
		int bannerH = game.over ? lh + 7 : 0;
		int guiH = mc.getWindow().getGuiScaledHeight();
		int avail = guiH - y - pad * 2 - headerH - bannerH - 4 - gap * (n - 1);
		int cell = Math.max(20, Math.min(cfg.cell, avail / n));

		int bw = n * cell + (n - 1) * gap;
		int pw = bw + pad * 2;
		int ph = pad + headerH + bw + pad + bannerH;

		g.fill(x, y, x + pw, y + ph, pal.bg());
		g.outline(x, y, pw, ph, pal.border());

		// Kopfzeile: Zeit, Modus
		String top = "Zeit " + formatTime(game.elapsed()) + "   " + n + "x" + n + (game.lockout ? " Lockout" : "");
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
				? "Bingo! Zeit " + formatTime(game.finalMs)
				: Bingo.names[Math.max(0, game.winner)] + " hat gewonnen! " + formatTime(game.finalMs);
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
