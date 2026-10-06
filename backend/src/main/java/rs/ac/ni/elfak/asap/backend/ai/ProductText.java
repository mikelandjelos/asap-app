package rs.ac.ni.elfak.asap.backend.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/** Embedding/TF-IDF text rules, identical to {@code ml/asap_ml/retrieval.py#product_text}. */
public final class ProductText {

    private static final Pattern WHITESPACE =
            Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS);

    private ProductText() {
    }

    /** {@code name | brand | category | deepest category labels | labels | description}, blanks omitted. */
    public static String full(String name, String brand, String category, List<String> categoryTags,
            String labels, String description) {
        List<String> parts = new ArrayList<>();
        add(parts, name);
        add(parts, brand);
        add(parts, category);
        add(parts, joinLabels(categoryTags));
        add(parts, labels);
        add(parts, description);
        return String.join(" | ", parts);
    }

    /** Product-type text used for clustering and the map; falls back to the name. */
    public static String type(String name, String category, List<String> categoryTags) {
        List<String> parts = new ArrayList<>();
        add(parts, category);
        add(parts, joinLabels(categoryTags));
        return parts.isEmpty() ? name : String.join(" | ", parts);
    }

    /** Normalized (name, brand) grouping package/size variants; same rule as {@code ranking.variant_key}. */
    public static String variantKey(String name, String brand) {
        return normalize(name) + "\u241f" + normalize(brand);
    }

    private static String normalize(String s) {
        return s == null ? "" : WHITESPACE.matcher(s.toLowerCase(Locale.ROOT)).replaceAll(" ").strip();
    }

    /** {@code en:plant-based-foods} becomes {@code plant based foods}. */
    public static String tagLabel(String tag) {
        int colon = tag.indexOf(':');
        return (colon >= 0 ? tag.substring(colon + 1) : tag).replace('-', ' ');
    }

    private static String joinLabels(List<String> tags) {
        if (tags == null) {
            return "";
        }
        return String.join(", ", tags.stream().map(ProductText::tagLabel).toList());
    }

    private static void add(List<String> parts, String value) {
        if (value != null && !value.isEmpty()) {
            parts.add(value);
        }
    }
}
