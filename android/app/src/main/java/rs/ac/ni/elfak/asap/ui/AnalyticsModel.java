package rs.ac.ni.elfak.asap.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import rs.ac.ni.elfak.asap.history.HistoryStore;

/** Personal analytics computed on the device from the local history only (S7d, D-039). Pure and unit-tested. */
public final class AnalyticsModel {

    public static final int DAYS = 14;
    public static final int TOP = 5;
    public static final int PERSONALIZE_AFTER = 3;

    public static final class Count {
        public final String label;
        public final int count;

        Count(String label, int count) {
            this.label = label;
            this.count = count;
        }
    }

    public final int scans;
    public final int distinctProducts;
    public final List<Count> topThemes;
    public final List<Count> sources;
    /** Oldest first; index {@code DAYS-1} is today. */
    public final int[] daily;
    public final List<double[]> historyPoints;
    public final double[] latest;
    public final double[] you;
    public final boolean youIsEstimate;

    private AnalyticsModel(int scans, int distinct, List<Count> topThemes, List<Count> sources, int[] daily,
            List<double[]> points, double[] latest, double[] you, boolean youIsEstimate) {
        this.scans = scans;
        this.distinctProducts = distinct;
        this.topThemes = topThemes;
        this.sources = sources;
        this.daily = daily;
        this.historyPoints = points;
        this.latest = latest;
        this.you = you;
        this.youIsEstimate = youIsEstimate;
    }

    public boolean personalized() {
        return distinctProducts >= PERSONALIZE_AFTER;
    }

    /**
     * @param entries newest-first history
     * @param serverYou exact "you" position from the latest v2 response (type-space centroid), or {@code null};
     *                  otherwise the mean of history points is shown and marked as an estimate
     */
    public static AnalyticsModel from(List<HistoryStore.Entry> entries, double[] serverYou, long nowMs, TimeZone zone) {
        Map<String, Integer> themes = new LinkedHashMap<>();
        Map<String, Integer> sources = new LinkedHashMap<>();
        Set<String> distinct = new HashSet<>();
        List<double[]> points = new ArrayList<>();
        int[] daily = new int[DAYS];
        long today = dayIndex(nowMs, zone);
        double sx = 0;
        double sy = 0;
        for (HistoryStore.Entry e : entries) {
            distinct.add(e.productId);
            increment(themes, e.themeLabel == null ? "Other" : e.themeLabel);
            increment(sources, sourceName(e.source));
            points.add(new double[] {e.x, e.y});
            sx += e.x;
            sy += e.y;
            long ago = today - dayIndex(e.occurredAtMs, zone);
            if (ago >= 0 && ago < DAYS) {
                daily[DAYS - 1 - (int) ago]++;
            }
        }
        double[] latest = entries.isEmpty() ? null : new double[] {entries.get(0).x, entries.get(0).y};
        double[] you = serverYou != null ? serverYou
                : entries.isEmpty() ? null : new double[] {sx / entries.size(), sy / entries.size()};
        return new AnalyticsModel(entries.size(), distinct.size(), top(themes, TOP), top(sources, Integer.MAX_VALUE),
                daily, Collections.unmodifiableList(points), latest, you, serverYou == null && you != null);
    }

    static String sourceName(String source) {
        if (source == null) {
            return "Unknown";
        }
        if ("catalog".equals(source)) {
            return "ASAP catalog";
        }
        List<String> names = new ArrayList<>();
        for (String part : source.split("\\+")) {
            names.add("upcitemdb".equals(part) ? "UPCitemdb" : "Open Food Facts (live)");
        }
        return names.size() == 1 ? names.get(0) : "Open Food Facts + UPCitemdb (live)";
    }

    private static void increment(Map<String, Integer> map, String key) {
        Integer v = map.get(key);
        map.put(key, v == null ? 1 : v + 1);
    }

    private static List<Count> top(Map<String, Integer> counts, int limit) {
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(counts.entrySet());
        Collections.sort(sorted, (a, b) -> {
            int byCount = Integer.compare(b.getValue(), a.getValue());
            return byCount != 0 ? byCount : a.getKey().compareTo(b.getKey());
        });
        List<Count> out = new ArrayList<>();
        for (Map.Entry<String, Integer> e : sorted) {
            if (out.size() == limit) {
                break;
            }
            out.add(new Count(e.getKey(), e.getValue()));
        }
        return Collections.unmodifiableList(out);
    }

    private static long dayIndex(long ms, TimeZone zone) {
        Calendar c = Calendar.getInstance(zone);
        c.setTimeInMillis(ms);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return (c.getTimeInMillis() + zone.getOffset(c.getTimeInMillis())) / (24L * 60 * 60 * 1000);
    }
}
