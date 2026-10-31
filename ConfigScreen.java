package dev.skyblockbingo;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Einstellungsmenue mit Reitern: Allgemein, Goals, Listen, Design, Spiel. */
public final class ConfigScreen extends Screen {
	private enum Tab {
		ALLGEMEIN("tab.general"), GOALS("tab.goals"), LISTEN("tab.lists"), DESIGN("tab.design"), SPIEL("tab.game");

		final String key;

		Tab(String key) {
			this.key = key;
		}
	}

	private static final int W = 320;
	private static final int PER_PAGE = 5;
	private static final int ROW = 21;
	private static final int WHITE = 0xFFFFFFFF;
	private static final int GRAY = 0xFFBBBBBB;

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
		super(Component.literal(Lang.t("menu.title")));
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
			Button b = button(Lang.t(t.key), x0 + i * (tw + 4), 10, tw, () -> {
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

		button(Lang.t("close"), (this.width - 120) / 2, this.height - 26, 120, this::onClose);
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

	private static String onOff(boolean b) {
		return Lang.t(b ? "onoff.on" : "onoff.off");
	}

	static String colorLabel(String key) {
		return switch (key) {
			case "p0" -> Lang.t("player1");
			case "p1" -> Lang.t("player2");
			default -> Lang.t("color." + key);
		};
	}

	// ---------------------------------------------------------------- Reiter: Allgemein

	private void buildGeneral() {
		Config cfg = Bingo.cfg;
		int half = (W - 6) / 2;

		button("-", x0 + W - 56, 44, 26, () -> {
			cfg.size = Math.max(3, cfg.size - 1);
			cfg.save();
		});
		button("+", x0 + W - 26, 44, 26, () -> {
			cfg.size = Math.min(7, cfg.size + 1);
			cfg.save();
		});

		button(Lang.t("gen.lockout", onOff(cfg.lockout)), x0, 70, half, (Button b) -> {
			cfg.lockout = !cfg.lockout;
			cfg.save();
			b.setMessage(Component.literal(Lang.t("gen.lockout", onOff(cfg.lockout))));
		});
		button(Lang.t("gen.hud", onOff(cfg.hudVisible)), x0 + half + 6, 70, half, (Button b) -> {
			cfg.hudVisible = !cfg.hudVisible;
			cfg.save();
			b.setMessage(Component.literal(Lang.t("gen.hud", onOff(cfg.hudVisible))));
		});

		button(Lang.t("gen.invclick", onOff(cfg.inventoryClick)), x0, 96, half, (Button b) -> {
			cfg.inventoryClick = !cfg.inventoryClick;
			cfg.save();
			b.setMessage(Component.literal(Lang.t("gen.invclick", onOff(cfg.inventoryClick))));
		});
		button(Lang.t("gen.lang", Lang.languageName()), x0 + half + 6, 96, half, () -> {
			int next = (Lang.index() + 1) % Lang.CODES.size();
			cfg.lang = Lang.CODES.get(next);
			cfg.save();
			this.rebuildWidgets();
		});

		button(Lang.t("gen.mover"), x0, 148, half, () -> {
			cfg.save();
			this.minecraft.gui.setScreen(new MoverScreen());
		});
		button(Lang.t("gen.openboard"), x0 + half + 6, 148, half, () -> {
			cfg.save();
			this.minecraft.gui.setScreen(new BoardScreen());
		});
	}

	// ---------------------------------------------------------------- Reiter: Goals

	private void buildGoals() {
		Config cfg = Bingo.cfg;
		goalBox = box(x0, 44, W - 100, "", 200, null);
		button(Lang.t("goals.add"), x0 + W - 96, 44, 96, () -> {
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
			setStatus(Lang.t("goals.added", added));
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
		button(Lang.t("goals.examples"), x0, py + 24, 154, () -> {
			int added = 0;
			for (String g : Lang.examples()) {
				if (!cfg.goals.contains(g)) {
					cfg.goals.add(g);
					added++;
				}
			}
			cfg.save();
			setStatus(Lang.t("goals.example_added", added));
			this.rebuildWidgets();
		});
		button(Lang.t("goals.clear"), x0 + W - 154, py + 24, 154, () -> {
			cfg.goals.clear();
			cfg.save();
			goalPage = 0;
			setStatus(Lang.t("goals.cleared"));
			this.rebuildWidgets();
		});
	}

	// ---------------------------------------------------------------- Reiter: Listen

	private void buildLists() {
		Config cfg = Bingo.cfg;
		listNames.clear();
		listNames.addAll(cfg.lists.keySet());

		listBox = box(x0, 44, W - 100, "", 30, null);
		button(Lang.t("lists.save"), x0 + W - 96, 44, 96, () -> {
			String n = listBox.getValue().trim();
			if (n.isEmpty()) {
				setStatus(Lang.t("lists.noname"));
				return;
			}
			if (cfg.goals.isEmpty()) {
				setStatus(Lang.t("lists.nogoals"));
				return;
			}
			cfg.lists.put(n, new ArrayList<>(cfg.goals));
			cfg.save();
			setStatus(Lang.t("lists.saved", n, cfg.goals.size()));
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
			button(Lang.t("lists.load"), x0 + W - 138, y, 66, () -> {
				List<String> l = cfg.lists.get(name);
				if (l != null) {
					cfg.goals = new ArrayList<>(l);
					cfg.save();
					setStatus(Lang.t("lists.loaded", name, l.size()));
				}
			});
			button(Lang.t("lists.delete"), x0 + W - 68, y, 68, () -> {
				cfg.lists.remove(name);
				cfg.save();
				setStatus(Lang.t("lists.deleted", name));
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
		String[] labels = {Lang.t("theme.dark"), Lang.t("theme.light"), Lang.t("theme.custom_long")};
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
		for (String key : Palette.KEYS) {
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
			// Anklickbares Farbfeld: oeffnet den Farbwaehler
			this.addRenderableWidget(Button.builder(Component.empty(), b -> {
				cfg.save();
				this.minecraft.gui.setScreen(new ColorPickerScreen(this, key));
			}).bounds(cx + 132, y + 2, 18, 16).build());
			i++;
		}
		button(Lang.t("gen.mover"), x0, 200, W, () -> {
			cfg.save();
			this.minecraft.gui.setScreen(new MoverScreen());
		});
	}

	// ---------------------------------------------------------------- Reiter: Spiel

	private void buildGame() {
		int half = (W - 8) / 2;
		button(Lang.t("game.solo"), x0, 44, half, () -> {
			Bingo.startSolo();
			setStatus(Lang.t("game.solo_status"));
		});
		button(Lang.t("game.host"), x0 + half + 8, 44, half, () -> {
			String code = Bingo.startHost();
			setStatus(Lang.t("game.host_status", code));
		});

		codeBox = box(x0 + 48, 70, 90, "", 6, null);
		button(Lang.t("game.join"), x0 + 144, 70, 100, () -> {
			String c = codeBox.getValue().trim();
			if (c.length() != 6) {
				setStatus(Lang.t("game.code_len"));
				return;
			}
			Bingo.join(c);
			setStatus(Lang.t("game.joining", c.toUpperCase()));
		});

		int third = (W - 12) / 3;
		button(Lang.t("game.start"), x0, 96, third, () -> {
			String err = Bingo.startGame();
			if (err != null) {
				setStatus(err);
			} else {
				this.minecraft.gui.setScreen(new BoardScreen());
			}
		});
		button(Lang.t("game.end"), x0 + third + 6, 96, third, () -> {
			Bingo.endGame();
			setStatus(Lang.t("game.ended"));
		});
		button(Lang.t("game.leave"), x0 + (third + 6) * 2, 96, third, () -> {
			Bingo.leave();
			setStatus(Lang.t("game.left"));
		});
		button(Lang.t("gen.openboard"), x0, 122, W, () -> this.minecraft.gui.setScreen(new BoardScreen()));
		button(Lang.t("game.public", Lang.t(cfg.publicLobby ? "onoff.on" : "onoff.off")), x0, 148, half, b -> {
			cfg.publicLobby = !cfg.publicLobby;
			cfg.save();
			b.setMessage(Component.literal(Lang.t("game.public", Lang.t(cfg.publicLobby ? "onoff.on" : "onoff.off"))));
		});
		button(Lang.t("game.browse"), x0 + half + 8, 148, half, () -> this.minecraft.gui.setScreen(new LobbyScreen(this)));
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
				text(g, Lang.t("gen.size", cfg.size, cfg.size), x0, 50, WHITE);
				text(g, Lang.t("gen.name"), x0, 128, WHITE);
				text(g, Bingo.ign(), x0 + 110, 128, 0xFF55FF55);
				text(g, Lang.t("gen.goals", cfg.goals.size(), Bingo.needed()), x0, 180, GRAY);
				text(g, Lang.t("gen.nextround"), x0, 192, GRAY);
				text(g, Lang.t("gen.invhint"), x0, 204, GRAY);
			}
			case GOALS -> {
				int start = goalPage * PER_PAGE;
				for (int r = 0; r < PER_PAGE; r++) {
					int idx = start + r;
					if (idx >= cfg.goals.size()) break;
					text(g, cut((idx + 1) + ". " + cfg.goals.get(idx), W - 32), x0 + 2, 70 + r * ROW + 6, WHITE);
				}
				if (cfg.goals.isEmpty()) text(g, cut(Lang.t("goals.empty"), W), x0 + 2, 76, GRAY);
				int pages = Math.max(1, (cfg.goals.size() + PER_PAGE - 1) / PER_PAGE);
				String p = Lang.t("goals.page", goalPage + 1, pages, cfg.goals.size(), Bingo.needed());
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
					text(g, cut(label, W - 144), x0 + 2, 70 + r * ROW + 6, WHITE);
				}
				if (listNames.isEmpty()) text(g, cut(Lang.t("lists.none"), W), x0 + 2, 76, GRAY);
				int pages = Math.max(1, (listNames.size() + PER_PAGE - 1) / PER_PAGE);
				String p = Lang.t("lists.page", listPage + 1, pages);
				text(g, p, (this.width - this.font.width(p)) / 2, 70 + PER_PAGE * ROW + 9, GRAY);
				text(g, cut(Lang.t("lists.info"), W), x0, 70 + PER_PAGE * ROW + 30, GRAY);
			}
			case DESIGN -> {
				Palette pal = Palette.of(cfg);
				int i = 0;
				for (String key : Palette.KEYS) {
					int cx = x0 + (i / 4) * 165;
					int y = 78 + (i % 4) * 26;
					text(g, colorLabel(key), cx, y + 6, WHITE);
					int col = 0xFF000000 | (pal.colorOf(key) & 0xFFFFFF);
					g.fill(cx + 133, y + 3, cx + 149, y + 17, col);
					g.outline(cx + 132, y + 2, 18, 16, 0xFFFFFFFF);
					i++;
				}
				text(g, cut(Lang.t("design.hex"), W), x0, 184, GRAY);
			}
			case SPIEL -> {
				text(g, Lang.t("game.code"), x0, 76, WHITE);
				int y = 176;
				String mode = Lang.t(switch (Bingo.role) {
					case NONE -> "mode.none";
					case SOLO -> "mode.solo";
					case HOST -> "mode.host";
					case GUEST -> "mode.guest";
				});
				text(g, Lang.t("game.mode", mode), x0, y, WHITE);
				if (!Bingo.code.isEmpty()) text(g, Lang.t("hud.code", Bingo.code), x0, y + 12, WHITE);
				if (Bingo.role == Bingo.Role.HOST) {
					text(g, Bingo.connected ? Lang.t("hud.peer_connected", Bingo.names[1]) : Lang.t("hud.waiting_peer"),
						x0, y + 24, GRAY);
				} else if (Bingo.role == Bingo.Role.GUEST) {
					text(g, Bingo.connected ? Lang.t("hud.connected_to", Bingo.names[0]) : Lang.t("hud.connecting"),
						x0, y + 24, GRAY);
				}
				text(g, Bingo.game == null ? Lang.t("game.norunning") : Lang.t("game.running", Bingo.game.size, Bingo.game.size),
					x0, y + 36, GRAY);
			}
		}

		if (!status.isEmpty()) {
			String s = cut(status, W + 60);
			text(g, s, (this.width - this.font.width(s)) / 2, this.height - 40, 0xFFFFD27F);
		}
	}

	@Override
	public void onClose() {
		Bingo.cfg.save();
		this.minecraft.gui.setScreen(null);
	}
}
