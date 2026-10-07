package net.hugoarb;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

public class ArbitrageScreen extends Screen {
    private static final int ROW_H = 12;
    private int scroll = 0;
    private int ticks = 0;

    public ArbitrageScreen() {
        super(Text.literal("HugoSMP Arbitrage"));
    }

    @Override
    protected void init() {
        HugoArbClient.refresh();
        addDrawableChild(ButtonWidget.builder(Text.literal("Refresh"), b -> HugoArbClient.refresh())
                .dimensions(width / 2 - 105, height - 28, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Schliessen"), b -> close())
                .dimensions(width / 2 + 5, height - 28, 100, 20).build());
    }

    @Override
    public void tick() {
        if (++ticks >= Config.get().refreshSeconds * 20) {
            ticks = 0;
            HugoArbClient.refresh();
        }
    }

    @Override
    public boolean shouldPause() {
        return false; // Spiel laeuft im Hintergrund weiter
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Gleiche Taste nochmal -> schliessen (nutzt die umgebundene Taste)
        if (HugoArbClient.OPEN_KEY.matchesKey(keyCode, scanCode)) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scroll -= (int) Math.signum(verticalAmount) * 3;
        return true;
    }

    private int visibleRows() {
        return Math.max(1, (height - 40 - 52) / ROW_H);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        int left = Math.max(10, width / 2 - 200);
        int right = Math.min(width - 10, width / 2 + 200);
        ctx.fill(left - 6, 18, right + 6, height - 34, 0xAA000000);

        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("HugoSMP Arbitrage"), width / 2, 24, 0xFFFFFF);

        long age = HugoArbClient.lastUpdate == 0 ? -1 : (System.currentTimeMillis() - HugoArbClient.lastUpdate) / 1000;
        String st = HugoArbClient.status + (age >= 0 ? "  (vor " + age + "s)" : "");
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal(st), width / 2, 36, 0xAAAAAA);

        int colName = left, colBuy = left + 150, colSell = left + 225, colProfit = left + 300;
        int y = 52;
        ctx.drawTextWithShadow(textRenderer, "Item", colName, y, 0xFFFF55);
        ctx.drawTextWithShadow(textRenderer, "Kauf", colBuy, y, 0xFFFF55);
        ctx.drawTextWithShadow(textRenderer, "Verkauf", colSell, y, 0xFFFF55);
        ctx.drawTextWithShadow(textRenderer, "Profit", colProfit, y, 0xFFFF55);
        y += ROW_H + 2;

        List<Entry> list = HugoArbClient.entries;
        int max = Math.min(list.size(), Config.get().maxEntries);
        int rows = visibleRows();
        scroll = Math.max(0, Math.min(scroll, Math.max(0, max - rows)));

        for (int i = scroll; i < Math.min(max, scroll + rows); i++) {
            Entry e = list.get(i);
            ctx.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(e.name(), 140), colName, y, 0xFFFFFF);
            ctx.drawTextWithShadow(textRenderer, fmt(e.buy()), colBuy, y, 0xFFFFFF);
            ctx.drawTextWithShadow(textRenderer, fmt(e.sell()), colSell, y, 0xFFFFFF);
            String p = fmt(e.profit()) + (e.percent() != null ? String.format(" (%.1f%%)", e.percent()) : "");
            int color = e.profit() != null && e.profit() > 0 ? 0x55FF55 : 0xFF5555;
            ctx.drawTextWithShadow(textRenderer, p, colProfit, y, color);
            y += ROW_H;
        }
    }

    private static String fmt(Double d) {
        if (d == null) return "-";
        return String.format("%,.0f", d);
    }
}
