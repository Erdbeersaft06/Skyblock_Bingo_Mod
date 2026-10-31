package dev.skyblockbingo;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Liste der oeffentlichen Lobbys mit Suche nach dem Namen des Hosts. */
public final class LobbyScreen extends Screen {
	private static final int ROWS = 7;
	private static final int ROW_H = 24;

	private final Screen parent;
	private List<Directory.Entry> all = new ArrayList<>();
	private boolean loading = true;
	private String error = null;
	private String filter = "";
	private int page = 0;
	private EditBox search;
	private int x0;
	private int w;

	public LobbyScreen(Screen parent) {
		super(Component.literal(Lang.t("lobby.title")));
		this.parent = parent;
	}

	private List<Directory.Entry> filtered() {
		String f = filter.toLowerCase(Locale.ROOT);
		List<Directory.Entry> out = new ArrayList<>();
		for (Directory.Entry e : all) {
			if (f.isEmpty() || e.host().toLowerCase(Locale.ROOT).contains(f)) out.add(e);
		}
		return out;
	}

	private void refresh() {
		loading = true;
		error = null;
		Directory.fetch(list -> {
			all = list;
			loading = false;
			page = 0;
			this.rebuildWidgets();
		}, err -> {
			loading = false;
			error = err;
		});
	}

	@Override
	protected void init() {
		w = Math.min(320, this.width - 20);
		x0 = (this.width - w) / 2;
		String old = search == null ? filter : search.getValue();
		search = new EditBox(this.font, x0, 34, w - 90, 20, Component.empty());
		search.setMaxLength(20);
		search.setHint(Component.literal(Lang.t("lobby.search")));
		search.setValue(old);
		search.setResponder(s -> {
			filter = s.trim();
			page = 0;
			this.rebuildWidgets();
		});
		this.addRenderableWidget(search);
		this.addRenderableWidget(Button.builder(Component.literal(Lang.t("lobby.refresh")), b -> {
			refresh();
		}).bounds(x0 + w - 84, 34, 84, 20).build());

		List<Directory.Entry> list = filtered();
		int pages = Math.max(1, (list.size() + ROWS - 1) / ROWS);
		page = Math.max(0, Math.min(page, pages - 1));
		for (int i = 0; i < ROWS; i++) {
			int idx = page * ROWS + i;
			if (idx >= list.size()) break;
			Directory.Entry e = list.get(idx);
			this.addRenderableWidget(Button.builder(Component.literal(Lang.t("lobby.join")), b -> {
				Bingo.join(e.code());
				this.minecraft.gui.setScreen(parent);
			}).bounds(x0 + w - 70, 62 + i * ROW_H, 70, 20).build());
		}
		int by = 62 + ROWS * ROW_H + 6;
		Button prev = Button.builder(Component.literal("<"), b -> {
			page--;
			this.rebuildWidgets();
		}).bounds(x0, by, 24, 20).build();
		prev.active = page > 0;
		this.addRenderableWidget(prev);
		Button next = Button.builder(Component.literal(">"), b -> {
			page++;
			this.rebuildWidgets();
		}).bounds(x0 + 30, by, 24, 20).build();
		next.active = page < pages - 1;
		this.addRenderableWidget(next);
		this.addRenderableWidget(Button.builder(Component.literal(Lang.t("lobby.back")), b -> this.onClose())
			.bounds(x0 + w - 100, by, 100, 20).build());
		if (loading && all.isEmpty() && error == null && !started) {
			started = true;
			refresh();
		}
	}

	private boolean started = false;

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		super.extractRenderState(g, mouseX, mouseY, delta);
		g.text(this.font, this.title.getString(), x0, 18, 0xFFFFFFFF, true);
		List<Directory.Entry> list = filtered();
		if (loading) {
			g.text(this.font, Lang.t("lobby.loading"), x0, 70, 0xFFBBBBBB, true);
		} else if (error != null) {
			g.text(this.font, Lang.t("lobby.error", error), x0, 70, 0xFFFF5555, true);
		} else if (list.isEmpty()) {
			g.text(this.font, Lang.t("lobby.none"), x0, 70, 0xFFBBBBBB, true);
		}
		for (int i = 0; i < ROWS; i++) {
			int idx = page * ROWS + i;
			if (idx >= list.size()) break;
			Directory.Entry e = list.get(idx);
			int y = 62 + i * ROW_H;
			g.fill(x0, y, x0 + w - 76, y + 20, 0x80000000);
			g.text(this.font, e.host(), x0 + 6, y + 6, 0xFFFFFFFF, true);
			String info = e.size() + "x" + e.size() + (e.lockout() ? " · Lockout" : "");
			g.text(this.font, info, x0 + w - 82 - this.font.width(info), y + 6, 0xFFBBBBBB, true);
		}
	}

	@Override
	public void onClose() {
		this.minecraft.gui.setScreen(parent);
	}
}
