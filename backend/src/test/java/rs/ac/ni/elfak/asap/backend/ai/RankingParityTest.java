package rs.ac.ni.elfak.asap.backend.ai;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.IOException;
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

/** Personalization, MMR, cluster and map parity against the bundle fixtures (D-031..D-034). */
@EnabledIf("rs.ac.ni.elfak.asap.backend.ai.BundleParityTest#bundleAvailable")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RankingParityTest {

    private RuntimeBundle bundle;
    private RecommendationEngine engine;
    private HybridRetriever retriever;
    private OnnxTextEncoder encoder;
    private Fixtures fx;

    @BeforeAll
    void load() throws IOException {
        bundle = RuntimeBundle.load(RuntimeBundle.current(BundleParityTest.root()));
        retriever = HybridRetriever.of(bundle);
        engine = new RecommendationEngine(bundle, retriever);
        encoder = OnnxTextEncoder.of(bundle);
        fx = JsonMapper.builder().build().readValue(bundle.dir().resolve("fixtures/parity.json").toFile(), Fixtures.class);
    }

    @AfterAll
    void close() throws Exception {
        if (encoder != null) {
            encoder.close();
        }
    }

    @Test
    void usersMatchPythonIncludingColdStartAndRepeats() {
        for (UserCase u : fx.users()) {
            List<Integer> history = Arrays.stream(u.historyIndices()).boxed().toList();
            RecommendationEngine.Result result = engine.recommend(u.queryIndex(), history);
            String mode = result.mode().name();
            assertEquals(u.mode(), mode, "mode for history size " + history.size());
            assertEquals("SUFFICIENT".equals(u.readiness()), result.historyState() == RecommendationEngine.HistoryState.APPLIED);
            int[] got = result.items().stream().mapToInt(RecommendationEngine.Ranked::index).toArray();
            assertSameRanking(u.mmrTop(), got, u);
            if (u.youXY() == null) {
                assertNull(result.you());
            } else {
                assertArrayEquals(u.youXY(), result.you(), fx.tolerances().xyAbs());
            }
            Set<Integer> excluded = new HashSet<>(history);
            excluded.add(u.queryIndex());
            assertTrue(Arrays.stream(got).noneMatch(excluded::contains), "history and query are never recommended");
        }
    }

    @Test
    void retrievalMmrMatchesPython() {
        for (RetrievalCase c : fx.retrieval()) {
            int[] got = engine.recommend(c.queryIndex(), List.of()).items().stream()
                    .mapToInt(RecommendationEngine.Ranked::index).toArray();
            assertArrayEquals(c.mmrTop(), got);
        }
    }

    @Test
    void uncataloguedProductsGetPythonClusterMapPositionAndResults() {
        for (NewProductCase c : fx.newProducts()) {
            float[] typeVector = encoder.encode(c.typeText());
            assertEquals(c.cluster(), PersonalRanker.nearestCluster(bundle.clusterCentroids(), typeVector), c.typeText());
            assertArrayEquals(c.xy(), engine.mapPosition(typeVector), fx.tolerances().xyAbs() * 10);
            double[] scores = retriever.relevance(encoder.encode(c.fullText()), c.fullText());
            int[] candidates = HybridRetriever.candidates(scores, Set.of(), bundle.manifest().params().candidatePool());
            int[] top = PersonalRanker.mmr(candidates, scores, bundle.embeddings(), bundle.manifest().params().mmrLambda(), 10);
            assertTrue(overlap(c.mmrTop(), top) >= 9, "top-10 overlap for " + c.fullText());
        }
    }

    @Test
    void catalogAssignmentAndThemePositionsMatchPython() {
        for (AssignmentCase a : fx.assignment()) {
            float[] t = bundle.typeEmbeddings().row(a.index());
            assertEquals(a.cluster(), PersonalRanker.nearestCluster(bundle.clusterCentroids(), t));
            assertArrayEquals(a.xy(), engine.catalogMapPosition(a.index()), fx.tolerances().xyAbs());
        }
        for (BundleModel.Theme theme : bundle.themes()) {
            double[] xy = engine.mapPosition(bundle.clusterCentroids().row(theme.cluster()));
            assertArrayEquals(new double[] {theme.x(), theme.y()}, xy, fx.tolerances().xyAbs());
        }
    }

    @Test
    void endToEndLatencyIsReported() {
        UserCase u = fx.users().get(fx.users().size() - 1);
        List<Integer> history = Arrays.stream(u.historyIndices()).boxed().toList();
        engine.recommend(u.queryIndex(), history);
        long t = System.nanoTime();
        for (int i = 0; i < 20; i++) {
            engine.recommend(u.queryIndex(), history);
        }
        System.out.printf("java personalized recommend (hybrid + profile + MMR + map) %.1f ms (mean of 20)%n", (System.nanoTime() - t) / 20e6);
    }

    private static int overlap(int[] a, int[] b) {
        Set<Integer> s = new HashSet<>(Arrays.stream(a).boxed().toList());
        return (int) Arrays.stream(b).filter(s::contains).count();
    }

    private void assertSameRanking(int[] expected, int[] got, UserCase u) {
        assertEquals(expected.length, got.length);
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != got[i]) {
                int ei = indexOf(u.candidates(), expected[i]);
                int gi = indexOf(u.candidates(), got[i]);
                assertTrue(ei >= 0 && gi >= 0 && Math.abs(u.scores()[ei] - u.scores()[gi]) < fx.tolerances().scoreAbs(),
                        "rank " + i + " differs beyond tie tolerance");
            }
        }
    }

    private static int indexOf(int[] a, int v) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] == v) {
                return i;
            }
        }
        return -1;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Fixtures(Tolerances tolerances, List<UserCase> users, List<RetrievalCase> retrieval,
            List<NewProductCase> newProducts, List<AssignmentCase> assignment) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Tolerances(double scoreAbs, double xyAbs) {
    }

    record UserCase(int[] historyIndices, int queryIndex, String readiness, String mode, int[] candidates,
            double[] scores, int[] mmrTop, double[] youXY) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record RetrievalCase(int queryIndex, int[] mmrTop) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record NewProductCase(String fullText, String typeText, int cluster, double[] xy, int[] mmrTop) {
    }

    record AssignmentCase(int index, int cluster, double[] xy) {
    }
}
