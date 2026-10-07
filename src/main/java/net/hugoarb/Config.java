package net.hugoarb;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("hugoarb.json");

    /** HIER die echte API-URL eintragen (siehe README: Browser DevTools -> Network). */
    public String apiUrl = "https://hugosmp-market.net/api/arbitrage";
    /** Optionaler Header, z.B. falls Market+ einen Key braucht. Leer lassen = kein Header. */
    public String authHeaderName = "";
    public String authHeaderValue = "";
    public int refreshSeconds = 10;
    public int maxEntries = 50;

    private static Config instance;

    public static Config get() {
        if (instance == null) instance = load();
        return instance;
    }

    private static Config load() {
        try {
            if (Files.exists(FILE)) {
                Config c = GSON.fromJson(Files.readString(FILE), Config.class);
                if (c != null) {
                    if (c.refreshSeconds < 3) c.refreshSeconds = 3; // niemanden spammen
                    return c;
                }
            }
        } catch (Exception e) {
            HugoArbClient.LOGGER.warn("Config konnte nicht gelesen werden", e);
        }
        Config c = new Config();
        c.save();
        return c;
    }

    public void save() {
        try {
            Files.writeString(FILE, GSON.toJson(this));
        } catch (Exception e) {
            HugoArbClient.LOGGER.warn("Config konnte nicht gespeichert werden", e);
        }
    }
}
