package rs.ac.ni.elfak.asap.history;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import rs.ac.ni.elfak.asap.network.I1ApiModels;
import rs.ac.ni.elfak.asap.network.V2ApiModels;

public class HistoryStoreTest {

    private static final long DAY = 24L * 60 * 60 * 1000;
    private File file;
    private long now;
    private int nextId;

    @Before
    public void setUp() throws Exception {
        file = new File(Files.createTempDirectory("history").toFile(), "history.json");
        now = 1_790_000_000_000L;
        nextId = 0;
    }

    private HistoryStore store() {
        return new HistoryStore(file, () -> now, () -> "e" + (nextId++));
    }

    static V2ApiModels.ProductData product(String id) {
        V2ApiModels.ProductData p = new V2ApiModels.ProductData();
        p.id = id;
        p.barcode = new I1ApiModels.BarcodeData("3017620422003", "EAN_13");
        p.name = "Name " + id;
        p.theme = new V2ApiModels.ThemeRef();
        p.theme.id = 21;
        p.theme.label = "Chocolate & hazelnut spreads";
        p.mapPosition = new V2ApiModels.MapPoint();
        p.mapPosition.x = 0.1;
        p.mapPosition.y = -0.2;
        return p;
    }

    @Test
    public void recordsNewestFirstAndPersistsAcrossInstances() {
        HistoryStore s = store();
        s.recordView(product("off:1"));
        now += 1000;
        s.recordView(product("off:2"));
        List<HistoryStore.Entry> reloaded = store().entries();
        assertEquals(2, reloaded.size());
        assertEquals("off:2", reloaded.get(0).productId);
        assertEquals("Chocolate & hazelnut spreads", reloaded.get(0).themeLabel);
    }

    @Test
    public void requestContextIsBoundedStrictlyOrderedAndMinimal() {
        HistoryStore s = store();
        for (int i = 0; i < 30; i++) {
            s.recordView(product("off:" + i)); // same clock: times are still forced strictly increasing
        }
        List<V2ApiModels.HistoryEvent> events = s.requestContext();
        assertEquals(HistoryStore.REQUEST_WINDOW, events.size());
        assertEquals("off:29", events.get(0).productId);
        assertEquals("PRODUCT_VIEWED", events.get(0).kind);
        for (int i = 1; i < events.size(); i++) {
            assertTrue(events.get(i - 1).occurredAt.compareTo(events.get(i).occurredAt) > 0);
        }
        assertTrue(events.get(0).occurredAt.endsWith("Z"));
    }

    @Test
    public void keepsAtMostFiftyEntriesAndDropsOlderThanNinetyDays() {
        HistoryStore s = store();
        for (int i = 0; i < 60; i++) {
            s.recordView(product("off:" + i));
        }
        assertEquals(HistoryStore.MAX_ENTRIES, s.entries().size());
        now += 91 * DAY;
        assertTrue(store().entries().isEmpty());
    }

    @Test
    public void clearRemovesEverythingAndCorruptFilesAreDiscarded() throws Exception {
        HistoryStore s = store();
        s.recordView(product("off:1"));
        s.clear();
        assertTrue(s.entries().isEmpty());
        assertFalse(file.exists());
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write("{not json".getBytes("UTF-8"));
        }
        assertTrue(store().entries().isEmpty());
    }

    @Test
    public void emptyHistoryProducesNoRequestField() {
        V2ApiModels.ScanQueryRequest request = new V2ApiModels.ScanQueryRequest("1", "EAN_13", store().requestContext());
        assertNull(request.history);
    }
}
