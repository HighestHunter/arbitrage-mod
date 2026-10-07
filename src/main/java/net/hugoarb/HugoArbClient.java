package net.hugoarb;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class HugoArbClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("hugoarb");
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();

    public static KeyBinding OPEN_KEY;

    public static volatile List<Entry> entries = List.of();
    public static volatile String status = "Noch nicht geladen";
    public static volatile long lastUpdate = 0;
    private static volatile boolean loading = false;

    @Override
    public void onInitializeClient() {
        Config.get();
        // Taste ist im Menue "Optionen > Steuerung > HugoSMP Arbitrage" frei umbelegbar.
        OPEN_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hugoarb.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.categories.hugoarb"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_KEY.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new ArbitrageScreen());
            }
        });
    }

    public static void refresh() {
        if (loading) return;
        loading = true;
        Config cfg = Config.get();
        try {
            HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(cfg.apiUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .header("User-Agent", "HugoSMP-Arbitrage-Mod/1.0");
            if (cfg.authHeaderName != null && !cfg.authHeaderName.isBlank())
                b.header(cfg.authHeaderName, cfg.authHeaderValue);

            HTTP.sendAsync(b.build(), HttpResponse.BodyHandlers.ofString())
                    .whenComplete((resp, err) -> {
                        try {
                            if (err != null) {
                                status = "Fehler: " + err.getMessage();
                            } else if (resp.statusCode() != 200) {
                                status = "HTTP " + resp.statusCode() + " - API-URL in config/hugoarb.json pruefen";
                            } else {
                                List<Entry> parsed = ArbitrageParser.parse(resp.body());
                                entries = parsed;
                                lastUpdate = System.currentTimeMillis();
                                status = parsed.isEmpty() ? "Antwort enthielt keine Eintraege" : "OK - " + parsed.size() + " Eintraege";
                            }
                        } catch (Exception e) {
                            status = "Parse-Fehler: " + e.getMessage();
                        } finally {
                            loading = false;
                        }
                    });
        } catch (Exception e) {
            status = "Ungueltige URL: " + e.getMessage();
            loading = false;
        }
    }
}
