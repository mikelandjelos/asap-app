package rs.ac.ni.elfak.asap.backend.ai;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIf;
import tools.jackson.databind.json.JsonMapper;

/**
 * Java ↔ Python parity against {@code fixtures/parity.json} of the current runtime bundle (D-034).
 * The bundle is git-ignored; these tests are skipped unless it exists (override with -Dasap.bundle.root).
 */
@EnabledIf("bundleAvailable")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BundleParityTest {

    private RuntimeBundle bundle;
    private Fixtures fx;
    private HybridRetriever retriever;
    private OnnxTextEncoder encoder;

    static boolean bundleAvailable() {
        return Files.exists(root().resolve("CURRENT"));
    }

    static Path root() {
        return Path.of(System.getProperty("asap.bundle.root", "../data/processed/bundle"));
    }

    @BeforeAll
    void load() throws IOException {
        long t = System.nanoTime();
        bundle = RuntimeBundle.load(RuntimeBundle.current(root()));
        retriever = HybridRetriever.of(bundle);
        encoder = OnnxTextEncoder.of(bundle);
        System.out.printf("bundle %s loaded and indexed in %.1f s%n", bundle.manifest().version(), (System.nanoTime() - t) / 1e9);
        fx = JsonMapper.builder().build().readValue(bundle.dir().resolve("fixtures/parity.json").toFile(), Fixtures.class);
    }

    @AfterAll
    void close() throws Exception {
        if (encoder != null) {
            encoder.close();
        }
    }

    @Test
    void tfidfMatchesSklearnIncludingUnicodeEdgeCases() {
        for (TfidfCase c : fx.tfidf()) {
            CharTfidf.SparseVector v = retriever.tfidf().transform(c.text());
            assertArrayEquals(c.indices(), v.indices(), "indices for: " + c.text());
            assertArrayEquals(c.values(), v.values(), fx.tolerances().tfidfAbs(), "values for: " + c.text());
        }
    }

    @Test
    void onnxEmbeddingsMatchPython() {
        for (EmbeddingCase c : fx.embedding()) {
            float[] got = encoder.encode(c.text());
            double cos = 0;
            for (int i = 0; i < got.length; i++) {
                cos += got[i] * c.vector()[i];
            }
            assertTrue(cos >= fx.tolerances().embeddingCosineMin(), "cosine " + cos + " for: " + c.text());
        }
    }

    @Test
    void hybridRetrievalMatchesPython() {
        var params = bundle.manifest().params();
        for (RetrievalCase c : fx.retrieval()) {
            double[] scores = retriever.relevance(c.queryIndex());
            // fixtures hold variant-collapsed candidates (S6c.1): one per variantKey, query's own variants removed
            Set<String> seen = new HashSet<>(Set.of(bundle.catalog().get(c.queryIndex()).variantKey()));
            int[] got = Arrays.stream(HybridRetriever.candidates(scores, Set.of(c.queryIndex()), params.variantPool()))
                    .filter(i -> seen.add(bundle.catalog().get(i).variantKey())).limit(params.candidatePool()).toArray();
            assertSameRanking(c.candidates(), got, scores);
            for (int i = 0; i < c.candidates().length; i++) {
                assertEquals(c.scores()[i], scores[c.candidates()[i]], fx.tolerances().scoreAbs());
            }
        }
    }

    @Test
    void uncataloguedProductTextAndEncodingMatchPython() {
        for (NewProductCase c : fx.newProducts()) {
            NewProduct p = c.product();
            String full = ProductText.full(p.name(), p.brand(), p.category(), p.categories(), p.labels(), p.description());
            assertEquals(c.fullText(), full);
            assertEquals(c.typeText(), ProductText.type(p.name(), p.category(), p.categories()));
            double[] scores = retriever.relevance(encoder.encode(full), full);
            int[] top = HybridRetriever.candidates(scores, Set.of(), 10);
            assertTrue(Set.of(Arrays.stream(top).boxed().toArray()).containsAll(
                    Arrays.stream(c.mmrTop()).limit(1).boxed().toList()), "best match retrieved for: " + full);
        }
    }

    @Test
    void latencyIsReported() {
        String text = bundle.catalog().get(123).fullText();
        encoder.encode(text);
        long t0 = System.nanoTime();
        for (int i = 0; i < 20; i++) {
            encoder.encode(text);
        }
        long t1 = System.nanoTime();
        for (int i = 0; i < 20; i++) {
            HybridRetriever.candidates(retriever.relevance(123), Set.of(123), 50);
        }
        long t2 = System.nanoTime();
        System.out.printf("java encode %.1f ms, hybrid top-50 over %d products %.1f ms (mean of 20)%n",
                (t1 - t0) / 20e6, bundle.catalog().size(), (t2 - t1) / 20e6);
    }

    private void assertSameRanking(int[] expected, int[] got, double[] scores) {
        assertEquals(expected.length, got.length);
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != got[i]) {
                assertTrue(Math.abs(scores[expected[i]] - scores[got[i]]) < fx.tolerances().scoreAbs(),
                        "rank " + i + " differs beyond tie tolerance");
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Fixtures(Tolerances tolerances, List<EmbeddingCase> embedding, List<TfidfCase> tfidf,
            List<RetrievalCase> retrieval, List<NewProductCase> newProducts) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Tolerances(double embeddingCosineMin, double tfidfAbs, double scoreAbs, double xyAbs) {
    }

    record EmbeddingCase(String text, double[] vector) {
    }

    record TfidfCase(String text, int[] indices, double[] values) {
    }

    record RetrievalCase(int queryIndex, int[] candidates, double[] scores, int[] mmrTop) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record NewProductCase(NewProduct product, String fullText, String typeText, int cluster, double[] xy, int[] mmrTop) {
    }

    record NewProduct(String name, String brand, String category, List<String> categories, String description, String labels) {
    }
}
