package rs.ac.ni.elfak.asap.backend.sources;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import rs.ac.ni.elfak.asap.backend.barcode.Barcode;
import rs.ac.ni.elfak.asap.backend.sources.SourceTypes.Found;
import rs.ac.ni.elfak.asap.backend.sources.SourceTypes.HttpFetcher;
import rs.ac.ni.elfak.asap.backend.sources.SourceTypes.NotFound;
import rs.ac.ni.elfak.asap.backend.sources.SourceTypes.Response;
import rs.ac.ni.elfak.asap.backend.sources.SourceTypes.Unavailable;

class SourcesTest {

    private static final Duration T = Duration.ofSeconds(1);
    private static final Barcode NUTELLA = new Barcode("3017620422003", Barcode.Format.EAN_13);
    private static final String OFF_FOUND = """
            {"code":"3017620422003","status":"success","product":{"product_name":"Nutella","brands":"Ferrero, Nutella",
             "categories_tags":["en:breakfasts","en:spreads","en:sweet-spreads","en:hazelnut-spreads","fr:pate"],
             "labels_tags":["en:no-gluten"],"generic_name":"Hazelnut spread"}}""";

    /** Records requested URLs and answers from a script. */
    static final class FakeHttp implements HttpFetcher {
        final List<String> urls = new ArrayList<>();
        final Function<String, Response> script;

        FakeHttp(Function<String, Response> script) {
            this.script = script;
        }

        @Override
        public Response get(String url, Duration timeout) throws IOException {
            urls.add(url);
            Response r = script.apply(url);
            if (r == null) {
                throw new IOException("connection refused");
            }
            return r;
        }
    }

    static Response json(int status, String body) {
        return new Response(status, "application/json; charset=utf-8", body);
    }

    @Test
    void upcEExpandsToUpcAAndRestrictedCodesAreDetected() {
        assertThat(SourceTypes.upcEToUpcA("04963406")).isEqualTo("049000006346"); // S2 corpus P02
        assertThat(SourceTypes.upcEToUpcA("01234531")).isEqualTo("012300000451");
        assertThat(SourceTypes.upcEToUpcA("01234541")).isEqualTo("012340000051");
        assertThat(SourceTypes.upcEToUpcA("01234571")).isEqualTo("012345000071");
        assertThat(SourceTypes.restricted(new Barcode("2000000000015", Barcode.Format.EAN_13))).isTrue();
        assertThat(SourceTypes.restricted(new Barcode("0204963406001", Barcode.Format.EAN_13))).isTrue();
        assertThat(SourceTypes.restricted(NUTELLA)).isFalse();
    }

    @Test
    void openFactsMapsFieldsAndClassifiesFailures() {
        OpenFactsSource off = new OpenFactsSource(new FakeHttp(u -> json(200, OFF_FOUND)), "https://off");
        Found f = (Found) off.lookup(NUTELLA, T);
        assertThat(f.record().name()).isEqualTo("Nutella");
        assertThat(f.record().brand()).isEqualTo("Ferrero");
        assertThat(f.record().category()).isEqualTo("Hazelnut spreads");
        assertThat(f.record().categoryTags()).containsExactly("en:spreads", "en:sweet-spreads", "en:hazelnut-spreads");
        assertThat(f.record().labels()).isEqualTo("no gluten");
        assertThat(f.record().description()).isEqualTo("Hazelnut spread");

        assertThat(new OpenFactsSource(new FakeHttp(u -> json(404, "{\"status\":\"failure\"}")), "x").lookup(NUTELLA, T))
                .isInstanceOf(NotFound.class);
        assertThat(new OpenFactsSource(new FakeHttp(u -> json(200, "{\"product\":{\"brands\":\"X\"}}")), "x").lookup(NUTELLA, T))
                .isInstanceOf(NotFound.class);
        assertThat(new OpenFactsSource(new FakeHttp(u -> json(429, "{}")), "x").lookup(NUTELLA, T)).isInstanceOf(Unavailable.class);
        assertThat(new OpenFactsSource(new FakeHttp(u -> new Response(502, "text/html", "<html>")), "x").lookup(NUTELLA, T))
                .isInstanceOf(Unavailable.class);
        assertThat(new OpenFactsSource(new FakeHttp(u -> null), "x").lookup(NUTELLA, T)).isInstanceOf(Unavailable.class);
    }

    @Test
    void upcItemDbExpandsUpcEMapsCategoryPathAndGuardsItsQuota() {
        FakeHttp http = new FakeHttp(u -> json(200, """
                {"code":"OK","items":[{"title":"Braun refill","brand":"Braun","category":"Health & Beauty > Shaving > Refills",
                 "description":"Cleaning fluid"}]}"""));
        Clock clock = Clock.fixed(Instant.parse("2026-10-06T12:00:00Z"), ZoneOffset.UTC);
        UpcItemDbSource upc = new UpcItemDbSource(http, "https://upc", 2, Duration.ofSeconds(11), clock);
        Found f = (Found) upc.lookup(new Barcode("04963406", Barcode.Format.UPC_E), T);
        assertThat(http.urls.get(0)).endsWith("upc=049000006346");
        assertThat(f.record().category()).isEqualTo("Refills");
        assertThat(upc.lookup(NUTELLA, T)).isInstanceOf(Unavailable.class); // spacing guard: no second call
        assertThat(http.urls).hasSize(1);

        assertThat(new UpcItemDbSource(new FakeHttp(u -> json(200, "{\"code\":\"OK\",\"items\":[]}")), "x", 5, Duration.ZERO, clock)
                .lookup(NUTELLA, T)).isInstanceOf(NotFound.class);
        assertThat(new UpcItemDbSource(new FakeHttp(u -> json(400, "{\"code\":\"INVALID_UPC\"}")), "x", 5, Duration.ZERO, clock)
                .lookup(NUTELLA, T)).isInstanceOf(NotFound.class);
        assertThat(new UpcItemDbSource(new FakeHttp(u -> new Response(502, "text/html", "<html>")), "x", 5, Duration.ZERO, clock)
                .lookup(NUTELLA, T)).isInstanceOf(Unavailable.class);
    }

    private static ProductRouter router(FakeHttp http) {
        Clock clock = Clock.systemUTC();
        return new ProductRouter(List.of(new OpenFactsSource(http, "https://off"),
                new UpcItemDbSource(http, "https://upc", 50, Duration.ZERO, clock)), Duration.ofSeconds(3), T, clock);
    }

    @Test
    void routerStopsEarlyWhenFirstSourceIsCompleteAndCachesOnlyOpenFacts() {
        FakeHttp http = new FakeHttp(u -> json(200, OFF_FOUND));
        ProductRouter router = router(http);
        ProductRouter.Resolution r = router.resolve(NUTELLA);
        assertThat(r.status()).isEqualTo(ProductRouter.Status.KNOWN);
        assertThat(r.sources()).containsExactly("open_food_facts");
        assertThat(http.urls).hasSize(1);
        router.resolve(NUTELLA);
        assertThat(http.urls).hasSize(1); // OFF result served from cache
    }

    @Test
    void routerMergesFieldsWithProvenanceAndLongestDescription() {
        FakeHttp http = new FakeHttp(u -> u.startsWith("https://off")
                ? json(200, "{\"product\":{\"product_name\":\"Mlinci\",\"categories_tags\":[\"en:pastas\"]}}")
                : json(200, "{\"code\":\"OK\",\"items\":[{\"title\":\"Mlinci pasta\",\"brand\":\"Podravka\",\"category\":\"Food\","
                        + "\"description\":\"Traditional baked pasta sheets\"}]}"));
        ProductRouter.Resolution r = router(http).resolve(new Barcode("3850334341389", Barcode.Format.EAN_13));
        assertThat(r.status()).isEqualTo(ProductRouter.Status.KNOWN);
        assertThat(r.merged().name()).isEqualTo("Mlinci");
        assertThat(r.merged().brand()).isEqualTo("Podravka");
        assertThat(r.merged().category()).isEqualTo("Pastas");
        assertThat(r.merged().description()).isEqualTo("Traditional baked pasta sheets");
        assertThat(r.fieldSources()).containsEntry("name", "open_food_facts").containsEntry("brand", "upcitemdb")
                .containsEntry("category", "open_food_facts").containsEntry("description", "upcitemdb");
        assertThat(r.sources()).containsExactly("open_food_facts", "upcitemdb");
        assertThat(r.merged().source()).isEqualTo("open_food_facts+upcitemdb");
    }

    @Test
    void routerClassifiesUnknownUnavailableRestrictedAndOpensItsCircuit() {
        FakeHttp notFound = new FakeHttp(u -> u.startsWith("https://off") ? json(404, "{}") : json(200, "{\"code\":\"OK\",\"items\":[]}"));
        assertThat(router(notFound).resolve(NUTELLA).status()).isEqualTo(ProductRouter.Status.UNKNOWN);

        FakeHttp partlyDown = new FakeHttp(u -> u.startsWith("https://off") ? json(404, "{}") : null);
        assertThat(router(partlyDown).resolve(NUTELLA).status()).isEqualTo(ProductRouter.Status.UNAVAILABLE);

        FakeHttp any = new FakeHttp(u -> json(200, OFF_FOUND));
        assertThat(router(any).resolve(new Barcode("2000000000015", Barcode.Format.EAN_13)).status())
                .isEqualTo(ProductRouter.Status.UNKNOWN);
        assertThat(any.urls).isEmpty(); // restricted code never leaves the server

        FakeHttp down = new FakeHttp(u -> null);
        ProductRouter router = router(down);
        for (int i = 0; i < 3; i++) {
            router.resolve(new Barcode("3017620422003", Barcode.Format.EAN_13));
        }
        int calls = down.urls.size();
        assertThat(router.resolve(NUTELLA).status()).isEqualTo(ProductRouter.Status.UNAVAILABLE);
        assertThat(down.urls).hasSize(calls); // both circuits open: no further calls
    }
}
