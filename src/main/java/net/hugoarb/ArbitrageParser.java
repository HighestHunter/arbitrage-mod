package net.hugoarb;

import com.google.gson.*;

import java.util.*;

/** Tolerant gegenueber unterschiedlichen JSON-Formaten: sucht ein Array von Objekten und raet die Felder. */
public class ArbitrageParser {
    private static final String[] NAME = {"item", "itemName", "item_name", "name", "displayName", "id", "material"};
    private static final String[] BUY = {"buy", "buyPrice", "buy_price", "ahPrice", "ah_price", "ah", "lowestBin", "price", "cost"};
    private static final String[] SELL = {"sell", "sellPrice", "sell_price", "orderPrice", "order_price", "order", "highestOrder", "revenue"};
    private static final String[] PROFIT = {"profit", "margin", "diff", "difference", "spread", "gain"};
    private static final String[] PERCENT = {"profitPercent", "profit_percent", "percent", "roi", "marginPercent", "margin_percent", "percentage"};
    private static final String[] CONTAINERS = {"data", "items", "results", "opportunities", "arbitrage", "entries", "list"};

    public static List<Entry> parse(String json) {
        JsonElement root = JsonParser.parseString(json);
        JsonArray arr = findArray(root);
        List<Entry> out = new ArrayList<>();
        if (arr == null) return out;
        for (JsonElement e : arr) {
            if (!e.isJsonObject()) continue;
            JsonObject o = e.getAsJsonObject();
            String name = str(o, NAME);
            Double buy = num(o, BUY);
            Double sell = num(o, SELL);
            Double profit = num(o, PROFIT);
            Double percent = num(o, PERCENT);
            if (profit == null && buy != null && sell != null) profit = sell - buy;
            if (percent == null && profit != null && buy != null && buy > 0) percent = profit / buy * 100.0;
            out.add(new Entry(name != null ? name : "?", buy, sell, profit, percent, o.toString()));
        }
        out.sort((a, b) -> Double.compare(b.profit() == null ? -1e18 : b.profit(), a.profit() == null ? -1e18 : a.profit()));
        return out;
    }

    private static JsonArray findArray(JsonElement el) {
        if (el == null) return null;
        if (el.isJsonArray()) return el.getAsJsonArray();
        if (!el.isJsonObject()) return null;
        JsonObject o = el.getAsJsonObject();
        for (String k : CONTAINERS) {
            if (o.has(k)) {
                JsonArray a = findArray(o.get(k));
                if (a != null) return a;
            }
        }
        for (Map.Entry<String, JsonElement> e : o.entrySet()) {
            JsonArray a = findArray(e.getValue());
            if (a != null) return a;
        }
        return null;
    }

    private static JsonElement first(JsonObject o, String[] keys) {
        for (String k : keys) {
            for (Map.Entry<String, JsonElement> e : o.entrySet()) {
                if (e.getKey().equalsIgnoreCase(k) && !e.getValue().isJsonNull()) return e.getValue();
            }
        }
        return null;
    }

    private static String str(JsonObject o, String[] keys) {
        JsonElement e = first(o, keys);
        if (e == null) return null;
        if (e.isJsonPrimitive()) return e.getAsString();
        if (e.isJsonObject()) {
            JsonElement n = first(e.getAsJsonObject(), NAME);
            if (n != null && n.isJsonPrimitive()) return n.getAsString();
        }
        return null;
    }

    private static Double num(JsonObject o, String[] keys) {
        JsonElement e = first(o, keys);
        if (e == null) return null;
        try {
            if (e.isJsonPrimitive()) return e.getAsDouble();
            if (e.isJsonObject()) { // z.B. {"price": 123}
                JsonElement p = first(e.getAsJsonObject(), new String[]{"price", "value", "amount"});
                if (p != null && p.isJsonPrimitive()) return p.getAsDouble();
            }
        } catch (Exception ignored) {}
        return null;
    }
}
