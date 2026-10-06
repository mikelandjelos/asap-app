package rs.ac.ni.elfak.asap.backend.api.v2;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.ni.elfak.asap.backend.ai.BundleModel.CatalogEntry;
import rs.ac.ni.elfak.asap.backend.ai.BundleModel.Theme;
import rs.ac.ni.elfak.asap.backend.ai.PersonalRanker;
import rs.ac.ni.elfak.asap.backend.ai.RecommendationEngine;
import rs.ac.ni.elfak.asap.backend.api.ApiContract.BarcodeData;
import rs.ac.ni.elfak.asap.backend.api.ApiContract.Provenance;
import rs.ac.ni.elfak.asap.backend.api.v2.V2Contract.CatalogMapResponse;
import rs.ac.ni.elfak.asap.backend.api.v2.V2Contract.Diversification;
import rs.ac.ni.elfak.asap.backend.api.v2.V2Contract.Evidence;
import rs.ac.ni.elfak.asap.backend.api.v2.V2Contract.MapPoint;
import rs.ac.ni.elfak.asap.backend.api.v2.V2Contract.ProductData;
import rs.ac.ni.elfak.asap.backend.api.v2.V2Contract.ProductPoint;
import rs.ac.ni.elfak.asap.backend.api.v2.V2Contract.ProductResponse;
import rs.ac.ni.elfak.asap.backend.api.v2.V2Contract.ProductSummary;
import rs.ac.ni.elfak.asap.backend.api.v2.V2Contract.RecommendationItem;
import rs.ac.ni.elfak.asap.backend.api.v2.V2Contract.RecommendationResponse;
import rs.ac.ni.elfak.asap.backend.api.v2.V2Contract.ScanQueryRequest;
import rs.ac.ni.elfak.asap.backend.api.v2.V2Contract.ScanQueryResponse;
import rs.ac.ni.elfak.asap.backend.api.v2.V2Contract.ThemePoint;
import rs.ac.ni.elfak.asap.backend.api.v2.V2Contract.ThemeRef;
import rs.ac.ni.elfak.asap.backend.api.v2.V2Contract.You;
import rs.ac.ni.elfak.asap.backend.barcode.BarcodeRules;

/** AI-backed v2 scan query and catalog map (docs/V2_CONTRACT.md). Registered only when a bundle is configured. */
@RestController
@RequestMapping("/api/v2")
@ConditionalOnProperty(name = "asap.bundle.dir")
public class V2Controller {

    private final RecommendationEngine engine;
    private final Map<Integer, Theme> themes;
    private final String modelVersion;
    private final String pipelineVersion;
    private final String attribution;

    public V2Controller(RecommendationEngine engine) {
        this.engine = engine;
        var bundle = engine.bundle();
        this.themes = bundle.themes().stream().collect(Collectors.toMap(Theme::cluster, Function.identity()));
        this.modelVersion = bundle.manifest().model().id() + "@" + bundle.manifest().model().revision().substring(0, 7);
        this.pipelineVersion = bundle.manifest().version();
        this.attribution = bundle.manifest().attribution();
    }

    @PostMapping(path = "/scan-queries", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ScanQueryResponse query(@Valid @RequestBody ScanQueryRequest request) {
        var barcode = BarcodeRules.validatedBarcode(request.barcode().value(), request.barcode().format());
        List<String> historyIds = HistoryValidator.validatedProductIds(request.history());
        List<Integer> history = engine.historyIndices(historyIds);
        var entry = engine.findByBarcode(barcode.value(), barcode.format().name());
        if (entry.isEmpty()) {
            You you = history.isEmpty() ? null : new You(history.size(), point(engine.mapPosition(
                    PersonalRanker.youVector(engine.bundle().typeEmbeddings(), history))));
            return new ScanQueryResponse(new ProductResponse("UNKNOWN", null),
                    new RecommendationResponse("NOT_APPLICABLE", null, "NOT_USED", pipelineVersion, null, null), you, attribution);
        }
        CatalogEntry product = entry.get();
        RecommendationEngine.Result result = engine.recommend(product.index(), history);
        String scoreType = result.mode() == RecommendationEngine.Mode.PERSONALIZED_HISTORY
                ? "PERSONALIZED_HYBRID_RELEVANCE" : "HYBRID_RELEVANCE";
        List<RecommendationItem> items = new ArrayList<>();
        for (int i = 0; i < result.items().size(); i++) {
            RecommendationEngine.Ranked ranked = result.items().get(i);
            CatalogEntry item = engine.bundle().catalog().get(ranked.index());
            items.add(new RecommendationItem(i + 1,
                    new ProductSummary(item.id(), barcode(item), item.name(), item.brand(), item.category()),
                    theme(item.cluster()), new Evidence(round(ranked.score()), scoreType, modelVersion)));
        }
        String status = items.isEmpty() ? "EMPTY" : "RESULTS";
        return new ScanQueryResponse(
                new ProductResponse("KNOWN", productData(product)),
                new RecommendationResponse(status, result.mode().name(), result.historyState().name(), pipelineVersion,
                        new Diversification("MMR", engine.bundle().manifest().params().mmrLambda()), items),
                result.you() == null ? null : new You(history.size(), point(result.you())),
                attribution);
    }

    @GetMapping(path = "/catalog-map", produces = MediaType.APPLICATION_JSON_VALUE)
    public CatalogMapResponse catalogMap() {
        var bundle = engine.bundle();
        return new CatalogMapResponse(pipelineVersion, attribution,
                bundle.themes().stream().map(t -> new ThemePoint(t.cluster(), t.label(), t.size(), t.x(), t.y())).toList(),
                bundle.mapSample().stream().map(p -> new ProductPoint(p.id(), p.cluster(), p.x(), p.y())).toList());
    }

    private ProductData productData(CatalogEntry p) {
        return new ProductData(p.id(), barcode(p), p.name(), p.brand(), p.category(), p.description(), p.tags(),
                new Provenance(p.provenance().type(), p.provenance().source()), theme(p.cluster()),
                point(engine.catalogMapPosition(p.index())));
    }

    private static BarcodeData barcode(CatalogEntry p) {
        return new BarcodeData(p.barcode().value(), p.barcode().format());
    }

    private ThemeRef theme(int cluster) {
        return new ThemeRef(cluster, themes.get(cluster).label());
    }

    private static MapPoint point(double[] xy) {
        return new MapPoint(round(xy[0]), round(xy[1]));
    }

    private static double round(double v) {
        return Math.round(v * 1e6) / 1e6;
    }
}
