package dev.skyblockbingo;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Oeffentliches Lobby-Verzeichnis: Hosts melden ihre offene Lobby auf einem gemeinsamen ntfy-Kanal. */
public final class Directory {
	private static final String URL = "https://ntfy.sh/sbbingo-lobbies";
	/** Eine Meldung gilt so lange (Sekunden); Hosts melden sich alle 20 Sekunden neu. */
	private static final long VALID_SECONDS = 70;
	private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

	public record Entry(String code, String host, int size, boolean lockout) { }

	private Directory() { }

	private static void post(JsonObject o) {
		Thread t = new Thread(() -> {
			try {
				HttpRequest req = HttpRequest.newBuilder(URI.create(URL))
					.timeout(Duration.ofSeconds(10))
					.header("Content-Type", "text/plain; charset=utf-8")
					.POST(HttpRequest.BodyPublishers.ofString(o.toString(), StandardCharsets.UTF_8))
					.build();
				HTTP.send(req, HttpResponse.BodyHandlers.discarding());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (Exception ignored) {
				// Verzeichnis ist optional
			}
		}, "skyblockbingo-dir-send");
		t.setDaemon(true);
		t.start();
	}

	public static void advertise(String code, String host, int size, boolean lockout) {
		JsonObject o = new JsonObject();
		o.addProperty("t", "ad");
		o.addProperty("code", code);
		o.addProperty("host", host);
		o.addProperty("size", size);
		o.addProperty("lockout", lockout);
		post(o);
	}

	public static void close(String code) {
		JsonObject o = new JsonObject();
		o.addProperty("t", "close");
		o.addProperty("code", code);
		post(o);
	}

	/** Laedt die aktuell offenen Lobbys. Das Ergebnis kommt auf dem Client-Thread an. */
	public static void fetch(Consumer<List<Entry>> done, Consumer<String> error) {
		Thread t = new Thread(() -> {
			try {
				HttpRequest req = HttpRequest.newBuilder(URI.create(URL + "/json?poll=1&since=5m"))
					.timeout(Duration.ofSeconds(10)).GET().build();
				HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
				if (resp.statusCode() >= 400) throw new IllegalStateException("HTTP " + resp.statusCode());
				long now = System.currentTimeMillis() / 1000;
				Map<String, Entry> open = new LinkedHashMap<>();
				BufferedReader br = new BufferedReader(new StringReader(resp.body()));
				String line;
				while ((line = br.readLine()) != null) {
					try {
						JsonObject j = JsonParser.parseString(line).getAsJsonObject();
						if (!j.has("event") || !"message".equals(j.get("event").getAsString())) continue;
						long time = j.has("time") ? j.get("time").getAsLong() : now;
						JsonObject m = JsonParser.parseString(j.get("message").getAsString()).getAsJsonObject();
						String code = m.get("code").getAsString();
						if ("close".equals(m.get("t").getAsString())) {
							open.remove(code);
						} else if (now - time <= VALID_SECONDS) {
							open.remove(code);
							String host = m.get("host").getAsString();
							if (host.length() > 20) host = host.substring(0, 20);
							int size = Math.max(3, Math.min(7, m.get("size").getAsInt()));
							open.put(code, new Entry(code, host, size, m.get("lockout").getAsBoolean()));
						} else {
							open.remove(code);
						}
					} catch (Exception ignored) {
						// kaputte Zeile ueberspringen
					}
				}
				List<Entry> list = new ArrayList<>(open.values());
				net.minecraft.client.Minecraft.getInstance().execute(() -> done.accept(list));
			} catch (Exception e) {
				net.minecraft.client.Minecraft.getInstance().execute(() -> error.accept(e.getClass().getSimpleName()));
			}
		}, "skyblockbingo-dir-fetch");
		t.setDaemon(true);
		t.start();
	}
}
