package dev.skyblockbingo;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Einstellungsmenue mit Reitern: Allgemein, Goals, Listen, Design, Spiel. */
public final class ConfigScreen extends Screen {
	private enum Tab {
		ALLGEMEIN("Allgemein"), GOALS("Goals"), LISTEN("Listen"), DESIGN("Design"), SPIEL("Spiel");

		final String label;

		Tab(String label) {
			this.label = label;
		}
	}

	private static final int W = 320;
	private static final int PER_PAGE = 5;
	private static final int ROW = 21;
	private static final int WHITE = 0xFFFFFFFF;
	private static final int GRAY = 0xFFBBBBBB;

	private static final Map<String, String> COLOR_LABELS = new LinkedHashMap<>();

	static {
		COLOR_LABELS.put("bg", "Hintergrund");
		COLOR_LABELS.put("cell", "Feld");
		COLOR_LABELS.put("text", "Text");
		COLOR_LABELS.put("muted", "Nebentext");
		COLOR_LABELS.put("p0", "Spieler 1");
		COLOR_LABELS.put("p1", "Spieler 2");
		COLOR_LABELS.put("border", "Rahmen");
	}

	private static Tab current = Tab.ALLGEMEIN;
	private static int goalPage = 0;
	private static int listPage = 0;

	private int x0;
	private String status = "";
	private EditBox goalBox;
	private EditBox listBox;
	private EditBox codeBox;
	private EditBox nameBox;
	private final List<String> listNames = new ArrayList<>();

	public ConfigScreen() {
		super(Component.literal("Skyblock Bingo - Einstellungen"));
	}

	// ---------------------------------------------------------------- Aufbau

	@Override
	protected void init() {
		x0 = (this.width - W) / 2;
		goalBox = null;
		listBox = null;
		codeBox = null;
		nameBox = null;

		// Reiter
		int tw = (W - 4 * 4) / 5;
		Tab[] tabs = Tab.values();
		for (int i = 0; i < tabs.length; i++) {
			Tab t = tabs[i];
			Button b = button(t.label, x0 + i * (tw + 4), 10, tw, () -> {
				current = t;
				this.rebuildWidgets();
			});
			b.active = (t != current);
		}

		switch (current) {
			case ALLGEMEIN -> buildGeneral();
			case GOALS -> buildGoals();
			case LISTEN -> buildLists();
			case DESIGN -> buildDesign();
			case SPIEL -> buildGame();
		}

		button("Schließen", (this.width - 120) / 2, this.height - 26, 120, this::onClose);
	}

	private Button button(String text, int x, int y, int w, Runnable action) {
		return button(text, x, y, w, b -> action.run());
	}

	private Button button(String text, int x, int y, int w, Consumer<Button> action) {
		Button b = Button.builder(Component.literal(text), btn -> action.accept(btn)).bounds(x, y, w, 20).build();
		this.addRenderableWidget(b);
		return b;
	}

	private EditBox box(int x, int y, int w, String value, int max, Consumer<String> responder) {
		EditBox e = new EditBox(this.font, x, y, w, 20, Component.empty());
		e.setMaxLength(max);
		e.setValue(value);
		if (responder != null) e.setResponder(responder);
		this.addRenderableWidget(e);
		return e;
	}

	private void setStatus(String s) {
		status = s;
	}

	// ---------------------------------------------------------------- Reiter: Allgemein

	private static String onOff(boolean b) {
		return b ? "An" : "Aus";
	}

	private void buildGeneral() {
		Config cfg = Bingo.cfg;
		button("-", x0 + W - 56, 44, 26, () -> {
			cfg.size = Math.max(3, cfg.size - 1);
			cfg.save();
		});
		button("+", x0 + W - 26, 44, 26, () -> {
			cfg.size = Math.min(7, cfg.size + 1);
			cfg.save();
		});
		button("Lockout: " + onOff(cfg.lockout), x0, 70, W, (Button b) -> {
			cfg.lockout = !cfg.lockout;
			cfg.save();
			b.setMessage(Component.literal("Lockout: " + onOff(cfg.lockout)));
		});
		button("HUD sichtbar: " + onOff(cfg.hudVisible), x0, 96, W, (Button b) -> {
			cfg.hudVisible = !cfg.hudVisible;
			cfg.save();
			b.setMessage(Component.literal("HUD sichtbar: " + onOff(cfg.hudVisible)));
		});
		nameBox = box(x0 + 110, 122, W - 110, cfg.name, 20, s -> cfg.name = s.trim());
		button("HUD verschieben und Größe ändern", x0, 148, W, () -> {
			cfg.save();
			this.minecraft.gui.setScreen(new MoverScreen());
		});
		button("Board öffnen (Felder abhaken)", x0, 174, W, () -> {
			cfg.save();
			this.minecraft.gui.setScreen(new BoardScreen());
		});
	}

	// ---------------------------------------------------------------- Reiter: Goals

	private void buildGoals() {
		Config cfg = Bingo.cfg;
		goalBox = box(x0, 44, W - 100, "", 200, null);
		button("Hinzufügen", x0 + W - 96, 44, 96, () -> {
			int added = 0;
			for (String part : goalBox.getValue().split(";")) {
				String g = part.trim();
				if (g.isEmpty()) continue;
				if (g.length() > Bingo.MAX_GOAL_LENGTH) g = g.substring(0, Bingo.MAX_GOAL_LENGTH);
				if (!cfg.goals.contains(g)) {
					cfg.goals.add(g);
					added++;
				}
			}
			cfg.save();
			setStatus(added + " Goal(s) hinzugefügt.");
			goalPage = Math.max(0, (cfg.goals.size() - 1) / PER_PAGE);
			this.rebuildWidgets();
		});

		int pages = Math.max(1, (cfg.goals.size() + PER_PAGE - 1) / PER_PAGE);
		goalPage = Math.max(0, Math.min(goalPage, pages - 1));
		int start = goalPage * PER_PAGE;
		for (int r = 0; r < PER_PAGE; r++) {
			int idx = start + r;
			if (idx >= cfg.goals.size()) break;
			button("×", x0 + W - 24, 70 + r * ROW, 24, () -> {
				cfg.goals.remove(idx);
				cfg.save();
				this.rebuildWidgets();
			});
		}
		int py = 70 + PER_PAGE * ROW + 3;
		button("<", x0, py, 30, () -> {
			goalPage = Math.max(0, goalPage - 1);
			this.rebuildWidgets();
		});
		button(">", x0 + W - 30, py, 30, () -> {
			goalPage = Math.min(pages - 1, goalPage + 1);
			this.rebuildWidgets();
		});
		button("Beispiele laden", x0, py + 24, 154, () -> {
			int added = 0;
			for (String g : BingoCommands.EXAMPLES) {
				if (!cfg.goals.contains(g)) {
					cfg.goals.add(g);
					added++;
				}
			}
			cfg.save();
			setStatus(added + " Beispiel-Goals hinzugefügt.");
			this.rebuildWidgets();
		});
		button("Alle löschen", x0 + W - 154, py + 24, 154, () -> {
			cfg.goals.clear();
			cfg.save();
			goalPage = 0;
			setStatus("Alle Goals gelöscht.");
			this.rebuildWidgets();
		});
	}

	// ---------------------------------------------------------------- Reiter: Listen

	private void buildLists() {
		Config cfg = Bingo.cfg;
		listNames.clear();
		listNames.addAll(cfg.lists.keySet());

		listBox = box(x0, 44, W - 100, "", 30, null);
		button("Speichern", x0 + W - 96, 44, 96, () -> {
			String n = listBox.getValue().trim();
			if (n.isEmpty()) {
				setStatus("Bitte einen Namen für die Liste eingeben.");
				return;
			}
			if (cfg.goals.isEmpty()) {
				setStatus("Es gibt noch keine Goals zum Speichern.");
				return;
			}
			cfg.lists.put(n, new ArrayList<>(cfg.goals));
			cfg.save();
			setStatus("Liste '" + n + "' gespeichert (" + cfg.goals.size() + " Goals).");
			this.rebuildWidgets();
		});

		int pages = Math.max(1, (listNames.size() + PER_PAGE - 1) / PER_PAGE);
		listPage = Math.max(0, Math.min(listPage, pages - 1));
		int start = listPage * PER_PAGE;
		for (int r = 0; r < PER_PAGE; r++) {
			int idx = start + r;
			if (idx >= listNames.size()) break;
			String name = listNames.get(idx);
			int y = 70 + r * ROW;
			button("Laden", x0 + W - 128, y, 60, () -> {
				List<String> l = cfg.lists.get(name);
				if (l != null) {
					cfg.goals = new ArrayList<>(l);
					cfg.save();
					setStatus("Liste '" + name + "' geladen (" + l.size() + " Goals).");
				}
			});
			button("Löschen", x0 + W - 64, y, 64, () -> {
				cfg.lists.remove(name);
				cfg.save();
				setStatus("Liste '" + name + "' gelöscht.");
				this.rebuildWidgets();
			});
		}
		int py = 70 + PER_PAGE * ROW + 3;
		button("<", x0, py, 30, () -> {
			listPage = Math.max(0, listPage - 1);
			this.rebuildWidgets();
		});
		button(">", x0 + W - 30, py, 30, () -> {
			listPage = Math.min(pages - 1, listPage + 1);
			this.rebuildWidgets();
		});
	}

	// ---------------------------------------------------------------- Reiter: Design

	private void buildDesign() {
		Config cfg = Bingo.cfg;
		String[] themes = {"dark", "light", "custom"};
		String[] labels = {"Dunkel", "Hell", "Eigene Farben"};
		for (int i = 0; i < 3; i++) {
			String t = themes[i];
			Button b = button(labels[i], x0 + i * 110, 44, 100, () -> {
				if (t.equals("custom")) Palette.ensureCustom(cfg);
				cfg.theme = t;
				cfg.save();
				this.rebuildWidgets();
			});
			b.active = !cfg.theme.equals(t);
		}

		Palette pal = Palette.of(cfg);
		int i = 0;
		for (String key : COLOR_LABELS.keySet()) {
			int cx = x0 + (i / 4) * 165;
			int y = 78 + (i % 4) * 26;
			box(cx + 72, y, 56, Palette.hexOf(pal, key), 7, s -> {
				String hex = s.startsWith("#") ? s.substring(1) : s;
				if (hex.matches("[0-9a-fA-F]{6}")) {
					Palette.ensureCustom(cfg);
					cfg.custom.put(key, hex.toUpperCase());
					cfg.theme = "custom";
				}
			});
			i++;
		}
		button("HUD verschieben und Größe ändern", x0, 190, W, () -> {
			cfg.save();
			this.minecraft.gui.setScreen(new MoverScreen());
		});
	}

	// ---------------------------------------------------------------- Reiter: Spiel

	private void buildGame() {
		int half = (W - 8) / 2;
		button("Alleine spielen", x0, 44, half, () -> {
			Bingo.startSolo();
			setStatus("Solo-Modus. Drücke \"Runde starten\".");
		});
		button("Party erstellen", x0 + half + 8, 44, half, () -> {
			String code = Bingo.startHost();
			setStatus("Party-Code: " + code + " (Mitspieler gibt ihn bei \"Beitreten\" ein)");
		});

		codeBox = box(x0 + 40, 70, 90, "", 6, null);
		button("Beitreten", x0 + 136, 70, 100, () -> {
			String c = codeBox.getValue().trim();
			if (c.length() != 6) {
				setStatus("Der Party-Code hat 6 Zeichen.");
				return;
			}
			Bingo.join(c);
			setStatus("Verbinde mit Party " + c.toUpperCase() + " ...");
		});

		int third = (W - 12) / 3;
		button("Runde starten", x0, 96, third, () -> {
			String err = Bingo.startGame();
			if (err != null) {
				setStatus(err);
			} else {
				this.minecraft.gui.setScreen(new BoardScreen());
			}
		});
		button("Runde beenden", x0 + third + 6, 96, third, () -> {
			Bingo.endGame();
			setStatus("Runde beendet.");
		});
		button("Verlassen", x0 + (third + 6) * 2, 96, third, () -> {
			Bingo.leave();
			setStatus("Spiel verlassen.");
		});
		button("Board öffnen (Felder abhaken)", x0, 122, W, () -> this.minecraft.gui.setScreen(new BoardScreen()));
	}

	// ---------------------------------------------------------------- Anzeige

	private void text(GuiGraphicsExtractor g, String s, int x, int y, int color) {
		g.text(this.font, s, x, y, color, false);
	}

	private String cut(String s, int maxW) {
		if (this.font.width(s) <= maxW) return s;
		String r = s;
		while (r.length() > 1 && this.font.width(r + "..") > maxW) r = r.substring(0, r.length() - 1);
		return r + "..";
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		super.extractRenderState(g, mouseX, mouseY, delta);
		Config cfg = Bingo.cfg;

		switch (current) {
			case ALLGEMEIN -> {
				text(g, "Board-Größe: " + cfg.size + " x " + cfg.size, x0, 50, WHITE);
				text(g, "Dein Name:", x0, 128, WHITE);
				text(g, "Goals: " + cfg.goals.size() + " (benötigt: " + Bingo.needed() + ")", x0, 204, GRAY);
				text(g, "Änderungen an Größe und Lockout gelten ab der nächsten Runde.", x0, 216, GRAY);
			}
			case GOALS -> {
				int start = goalPage * PER_PAGE;
				for (int r = 0; r < PER_PAGE; r++) {
					int idx = start + r;
					if (idx >= cfg.goals.size()) break;
					text(g, cut((idx + 1) + ". " + cfg.goals.get(idx), W - 32), x0 + 2, 70 + r * ROW + 6, WHITE);
				}
				if (cfg.goals.isEmpty()) text(g, "Noch keine Goals. Oben eintippen (mehrere mit ; trennen).", x0 + 2, 76, GRAY);
				int pages = Math.max(1, (cfg.goals.size() + PER_PAGE - 1) / PER_PAGE);
				String p = "Seite " + (goalPage + 1) + " / " + pages + "   Goals: " + cfg.goals.size() + " / " + Bingo.needed();
				text(g, p, (this.width - this.font.width(p)) / 2, 70 + PER_PAGE * ROW + 9, GRAY);
			}
			case LISTEN -> {
				int start = listPage * PER_PAGE;
				for (int r = 0; r < PER_PAGE; r++) {
					int idx = start + r;
					if (idx >= listNames.size()) break;
					String n = listNames.get(idx);
					List<String> l = cfg.lists.get(n);
					String label = n + " (" + (l == null ? 0 : l.size()) + ")";
					text(g, cut(label, W - 136), x0 + 2, 70 + r * ROW + 6, WHITE);
				}
				if (listNames.isEmpty()) text(g, "Noch keine Listen. Name eingeben und Speichern klicken.", x0 + 2, 76, GRAY);
				int pages = Math.max(1, (listNames.size() + PER_PAGE - 1) / PER_PAGE);
				String p = "Seite " + (listPage + 1) + " / " + pages;
				text(g, p, (this.width - this.font.width(p)) / 2, 70 + PER_PAGE * ROW + 9, GRAY);
				text(g, "Speichert die aktuellen Goals unter dem Namen.", x0, 70 + PER_PAGE * ROW + 30, GRAY);
			}
			case DESIGN -> {
				Palette pal = Palette.of(cfg);
				int i = 0;
				for (Map.Entry<String, String> e : COLOR_LABELS.entrySet()) {
					int cx = x0 + (i / 4) * 165;
					int y = 78 + (i % 4) * 26;
					text(g, e.getValue(), cx, y + 6, WHITE);
					int col = 0xFF000000 | (pal.colorOf(e.getKey()) & 0xFFFFFF);
					g.fill(cx + 134, y + 3, cx + 148, y + 17, col);
					g.outline(cx + 134, y + 3, 14, 14, 0xFFFFFFFF);
					i++;
				}
				text(g, "Eigene Farben als Hex-Wert (z. B. FF8800) eintippen.", x0, 168, GRAY);
			}
			case SPIEL -> {
				text(g, "Code:", x0, 76, WHITE);
				int y = 152;
				text(g, "Modus: " + switch (Bingo.role) {
					case NONE -> "kein Spiel";
					case SOLO -> "Solo";
					case HOST -> "Host";
					case GUEST -> "Mitspieler";
				}, x0, y, WHITE);
				if (!Bingo.code.isEmpty()) text(g, "Party-Code: " + Bingo.code, x0, y + 12, WHITE);
				if (Bingo.role == Bingo.Role.HOST) {
					text(g, Bingo.connected ? Bingo.names[1] + " ist verbunden" : "Warte auf Mitspieler ...", x0, y + 24, GRAY);
				} else if (Bingo.role == Bingo.Role.GUEST) {
					text(g, Bingo.connected ? "Verbunden mit " + Bingo.names[0] : "Verbinde ...", x0, y + 24, GRAY);
				}
				text(g, Bingo.game == null ? "Keine Runde aktiv" : "Runde läuft: " + Bingo.game.size + "x" + Bingo.game.size, x0, y + 36, GRAY);
			}
		}

		if (!status.isEmpty()) {
			text(g, cut(status, W), (this.width - this.font.width(cut(status, W))) / 2, this.height - 40, 0xFFFFD27F);
		}
	}

	@Override
	public void onClose() {
		Bingo.cfg.save();
		this.minecraft.gui.setScreen(null);
	}
}
