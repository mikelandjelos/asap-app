package rs.ac.ni.elfak.asap.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import rs.ac.ni.elfak.asap.ScanSession;
import rs.ac.ni.elfak.asap.network.I1ApiModels;
import rs.ac.ni.elfak.asap.network.ScanQueryClient;
import rs.ac.ni.elfak.asap.network.V2ApiModels;

/** Pure mapping from a {@link ScanSession.State} to what the Product screen shows (unit-tested, no Android types). */
public final class ProductUiModel {

    public enum Screen { NONE, LOADING, PRODUCT, NOT_FOUND, UNAVAILABLE, OFFLINE, SERVER_ERROR, INVALID_RESPONSE }

    public enum ResultsKind { PERSONAL, SIMILAR, EMPTY }

    public static final class Row {
        public final int rank;
        public final String title;
        public final String subtitle;
        public final String theme;
        public final String barcodeValue;
        public final String barcodeFormat;
        public final String imageUrl;

        Row(int rank, String title, String subtitle, String theme, String barcodeValue, String barcodeFormat,
                String imageUrl) {
            this.rank = rank;
            this.title = title;
            this.subtitle = subtitle;
            this.theme = theme;
            this.barcodeValue = barcodeValue;
            this.barcodeFormat = barcodeFormat;
            this.imageUrl = imageUrl;
        }
    }

    public final Screen screen;
    public final String barcode;
    public final boolean retry;
    public final Integer httpStatus;
    public final String name;
    public final String details;
    public final String description;
    public final String theme;
    public final String source;
    public final ResultsKind resultsKind;
    public final int historyUsed;
    public final List<Row> rows;
    public final String attribution;
    public final String imageUrl;

    private ProductUiModel(Screen screen, String barcode, boolean retry, Integer httpStatus, String name, String details,
            String description, String theme, String source, ResultsKind resultsKind, int historyUsed, List<Row> rows,
            String attribution, String imageUrl) {
        this.screen = screen;
        this.barcode = barcode;
        this.retry = retry;
        this.httpStatus = httpStatus;
        this.name = name;
        this.details = details;
        this.description = description;
        this.theme = theme;
        this.source = source;
        this.resultsKind = resultsKind;
        this.historyUsed = historyUsed;
        this.rows = rows;
        this.attribution = attribution;
        this.imageUrl = imageUrl;
    }

    private static ProductUiModel simple(Screen screen, String barcode, boolean retry, Integer status) {
        return new ProductUiModel(screen, barcode, retry, status, null, null, null, null, null, null, 0,
                Collections.<Row>emptyList(), null, null);
    }

    public static ProductUiModel from(ScanSession.State state) {
        switch (state.phase) {
            case LOADING:
                return simple(Screen.LOADING, state.barcode, false, null);
            case FAILED:
                return failure(state.barcode, state.failure);
            case LOADED:
                return loaded(state.barcode, state.response);
            default:
                return simple(Screen.NONE, null, false, null);
        }
    }

    private static ProductUiModel failure(String barcode, ScanQueryClient.Failure failure) {
        switch (failure.kind()) {
            case TRANSPORT:
                return simple(Screen.OFFLINE, barcode, true, null);
            case HTTP:
                return simple(Screen.SERVER_ERROR, barcode, true, failure.httpStatus());
            default:
                return simple(Screen.INVALID_RESPONSE, barcode, true, null);
        }
    }

    private static ProductUiModel loaded(String barcode, V2ApiModels.ScanQueryResponse r) {
        if ("UNKNOWN".equals(r.product.status)) {
            return simple(Screen.NOT_FOUND, barcode, false, null);
        }
        if ("UNAVAILABLE".equals(r.product.status)) {
            return simple(Screen.UNAVAILABLE, barcode, true, null);
        }
        V2ApiModels.ProductData p = r.product.data;
        List<Row> rows = new ArrayList<>();
        if (r.recommendations.items != null) {
            for (V2ApiModels.Item item : r.recommendations.items) {
                V2ApiModels.Summary s = item.product;
                rows.add(new Row(item.rank, s.name, join(s.brand, s.category), item.theme.label,
                        s.barcode == null ? null : s.barcode.value, s.barcode == null ? null : s.barcode.format, s.imageUrl));
            }
        }
        ResultsKind kind = rows.isEmpty() ? ResultsKind.EMPTY
                : "PERSONALIZED_HISTORY".equals(r.recommendations.mode) ? ResultsKind.PERSONAL : ResultsKind.SIMILAR;
        return new ProductUiModel(Screen.PRODUCT, barcode, false, null, p.name, join(p.brand, p.category), p.description,
                p.theme.label, sourceLabel(p.provenance), kind, r.you == null ? 0 : r.you.historyUsed,
                Collections.unmodifiableList(rows), r.attribution, p.imageUrl);
    }

    /** Human-readable data origin: catalog vs live provider(s). */
    static String sourceLabel(I1ApiModels.Provenance provenance) {
        List<String> names = new ArrayList<>();
        for (String part : provenance.source.split("\\+")) {
            names.add(providerName(part));
        }
        String joined = joinAll(" + ", names);
        return "EXTERNAL_PROVIDER".equals(provenance.type) ? joined + " (live lookup)" : "ASAP catalog · " + joined;
    }

    private static String providerName(String id) {
        switch (id) {
            case "open_food_facts":
                return "Open Food Facts";
            case "open_beauty_facts":
                return "Open Beauty Facts";
            case "open_pet_food_facts":
                return "Open Pet Food Facts";
            case "upcitemdb":
                return "UPCitemdb";
            default:
                return id;
        }
    }

    private static String join(String a, String b) {
        boolean hasA = a != null && !a.trim().isEmpty();
        boolean hasB = b != null && !b.trim().isEmpty();
        if (hasA && hasB) {
            return a + " · " + b;
        }
        return hasA ? a : hasB ? b : null;
    }

    private static String joinAll(String sep, List<String> parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (sb.length() > 0) {
                sb.append(sep);
            }
            sb.append(p);
        }
        return sb.toString();
    }
}
