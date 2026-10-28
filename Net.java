package dev.skyblockbingo;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Einfacher Nachrichtenkanal ueber den oeffentlichen Dienst ntfy.sh.
 * Der Party-Code ist der Name des Kanals. Es wird kein eigener Server benoetigt.
 */
public final class Net {
	private static final String BASE = "https://ntfy.sh/";

	private final String topic;
	private final Consumer<JsonObject> onMessage;
	private final Consumer<String> onError;
	private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
	private final ExecutorService publisher = Executors.newSingleThreadExecutor(r -> {
		Thread t = new Thread(r, "skyblockbingo-send");
		t.setDaemon(true);
		return t;
	});
	private final Set<String> seen = new HashSet<>();
	private volatile boolean running = true;
	private volatile InputStream current;
	private Thread reader;

	public Net(String code, Consumer<JsonObject> onMessage, Consumer<String> onError) {
		this.topic = "sbbingo-" + code;
		this.onMessage = onMessage;
		this.onError = onError;
	}

	public void start() {
		reader = new Thread(this::loop, "skyblockbingo-recv");
		reader.setDaemon(true);
		reader.start();
	}

	public void send(JsonObject o) {
		final String body = o.toString();
		publisher.execute(() -> {
			try {
				HttpRequest req = HttpRequest.newBuilder(URI.create(BASE + topic))
					.timeout(Duration.ofSeconds(10))
					.header("Content-Type", "text/plain; charset=utf-8")
					.POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
					.build();
				HttpResponse<Void> resp = http.send(req, HttpResponse.BodyHandlers.discarding());
				if (!running) return;
				if (resp.statusCode() == 429) {
					onError.accept("Zu viele Nachrichten, bitte kurz warten.");
				} else if (resp.statusCode() >= 400) {
					onError.accept("Senden fehlgeschlagen (HTTP " + resp.statusCode() + ").");
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				if (running) onError.accept("Senden fehlgeschlagen: " + e.getClass().getSimpleName());
			}
		});
	}

	private void loop() {
		boolean reportedError = false;
		while (running) {
			try {
				HttpRequest req = HttpRequest.newBuilder(URI.create(BASE + topic + "/json?since=all")).GET().build();
				HttpResponse<InputStream> resp = http.send(req, HttpResponse.BodyHandlers.ofInputStream());
				if (resp.statusCode() >= 400) {
					throw new IllegalStateException("HTTP " + resp.statusCode());
				}
				reportedError = false;
				current = resp.body();
				try (BufferedReader br = new BufferedReader(new InputStreamReader(current, StandardCharsets.UTF_8))) {
					String line;
					while (running && (line = br.readLine()) != null) {
						handleLine(line);
					}
				}
			} catch (InterruptedException e) {
				return;
			} catch (Exception e) {
				if (!running) return;
				if (!reportedError) {
					reportedError = true;
					onError.accept("Verbindung unterbrochen, versuche erneut ...");
				}
			}
			if (running) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					return;
				}
			}
		}
	}

	private void handleLine(String line) {
		try {
			JsonObject j = JsonParser.parseString(line).getAsJsonObject();
			if (!j.has("event") || !"message".equals(j.get("event").getAsString())) return;
			String id = j.has("id") ? j.get("id").getAsString() : null;
			if (id != null && !seen.add(id)) return;
			JsonObject msg = JsonParser.parseString(j.get("message").getAsString()).getAsJsonObject();
			onMessage.accept(msg);
		} catch (Exception ignored) {
			// fremde oder kaputte Nachricht ignorieren
		}
	}

	public void close() {
		running = false;
		publisher.shutdown();
		try {
			InputStream in = current;
			if (in != null) in.close();
		} catch (Exception ignored) {
			// egal
		}
		if (reader != null) reader.interrupt();
	}
}
