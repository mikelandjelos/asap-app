package rs.ac.ni.elfak.asap.ui;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import org.junit.Test;
import rs.ac.ni.elfak.asap.history.HistoryStore;

public class AnalyticsModelTest {

    private static final long DAY = 24L * 60 * 60 * 1000;
    private static final long NOW = 1_790_000_000_000L;
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    private static HistoryStore.Entry entry(String id, String theme, String source, long at, double x, double y) {
        HistoryStore.Entry e = new HistoryStore.Entry();
        e.productId = id;
        e.themeLabel = theme;
        e.source = source;
        e.occurredAtMs = at;
        e.x = x;
        e.y = y;
        return e;
    }

    @Test
    public void emptyHistoryHasNoYouAndIsNotPersonalized() {
        AnalyticsModel m = AnalyticsModel.from(Collections.<HistoryStore.Entry>emptyList(), null, NOW, UTC);
        assertEquals(0, m.scans);
        assertNull(m.you);
        assertNull(m.latest);
        assertFalse(m.personalized());
        assertTrue(m.topThemes.isEmpty());
    }

    @Test
    public void countsThemesSourcesDaysAndPersonalizationFromDistinctProducts() {
        List<HistoryStore.Entry> h = new ArrayList<>();
        h.add(entry("a", "Spreads", "catalog", NOW, 1, 1));               // newest: today
        h.add(entry("a", "Spreads", "catalog", NOW - DAY, 3, 3));         // repeat of a, yesterday
        h.add(entry("b", "Beer", "open_food_facts", NOW - 2 * DAY, 2, 2));
        h.add(entry("c", "Spreads", "upcitemdb", NOW - 30 * DAY, 0, 0));  // outside the 14-day window
        AnalyticsModel m = AnalyticsModel.from(h, null, NOW, UTC);
        assertEquals(4, m.scans);
        assertEquals(3, m.distinctProducts);
        assertTrue(m.personalized());
        assertEquals("Spreads", m.topThemes.get(0).label);
        assertEquals(3, m.topThemes.get(0).count);
        assertEquals("ASAP catalog", m.sources.get(0).label);
        assertEquals(1, m.daily[AnalyticsModel.DAYS - 1]);
        assertEquals(1, m.daily[AnalyticsModel.DAYS - 2]);
        assertEquals(1, m.daily[AnalyticsModel.DAYS - 3]);
        assertArrayEquals(new double[] {1, 1}, m.latest, 0);
        assertArrayEquals(new double[] {1.5, 1.5}, m.you, 1e-9); // mean of points: an estimate
        assertTrue(m.youIsEstimate);
    }

    @Test
    public void serverYouWinsOverTheEstimateAndSourceNamesAreReadable() {
        List<HistoryStore.Entry> h = Collections.singletonList(entry("a", "Water", "open_food_facts+upcitemdb", NOW, 1, 1));
        AnalyticsModel m = AnalyticsModel.from(h, new double[] {0.2, -0.1}, NOW, UTC);
        assertArrayEquals(new double[] {0.2, -0.1}, m.you, 0);
        assertFalse(m.youIsEstimate);
        assertEquals("Open Food Facts + UPCitemdb (live)", m.sources.get(0).label);
        assertEquals("Unknown", AnalyticsModel.sourceName(null));
    }
}
