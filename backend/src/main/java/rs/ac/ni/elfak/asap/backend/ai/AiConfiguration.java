package rs.ac.ni.elfak.asap.backend.ai;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rs.ac.ni.elfak.asap.backend.sources.JavaHttpFetcher;
import rs.ac.ni.elfak.asap.backend.sources.OpenFactsSource;
import rs.ac.ni.elfak.asap.backend.sources.ProductRouter;
import rs.ac.ni.elfak.asap.backend.sources.SourceTypes.HttpFetcher;
import rs.ac.ni.elfak.asap.backend.sources.SourceTypes.ProductSource;
import rs.ac.ni.elfak.asap.backend.sources.UpcItemDbSource;

/**
 * AI pipeline beans, created only when {@code asap.bundle.dir} points at a bundle directory or at a bundle root
 * containing {@code CURRENT}. Without it the application serves v1 only.
 */
@Configuration
@ConditionalOnProperty(name = "asap.bundle.dir")
public class AiConfiguration {

    @Bean
    RuntimeBundle runtimeBundle(@Value("${asap.bundle.dir}") String dir) throws IOException {
        Path path = Path.of(dir);
        return RuntimeBundle.load(Files.exists(path.resolve("CURRENT")) ? RuntimeBundle.current(path) : path);
    }

    @Bean
    HybridRetriever hybridRetriever(RuntimeBundle bundle) {
        return HybridRetriever.of(bundle);
    }

    @Bean(destroyMethod = "close")
    OnnxTextEncoder onnxTextEncoder(RuntimeBundle bundle) throws IOException {
        return OnnxTextEncoder.of(bundle);
    }

    @Bean
    RecommendationEngine recommendationEngine(RuntimeBundle bundle, HybridRetriever retriever) {
        return new RecommendationEngine(bundle, retriever);
    }

    @Bean
    HttpFetcher httpFetcher(@Value("${asap.sources.user-agent:ASAP/0.1 (https://github.com/mikelandjelos/asap-app)}") String userAgent) {
        return new JavaHttpFetcher(userAgent);
    }

    /** Live product sources (D-029). Disable with {@code asap.sources.enabled=false}: only the catalog resolves. */
    @Bean
    ProductRouter productRouter(
            HttpFetcher http,
            @Value("${asap.sources.enabled:true}") boolean enabled,
            @Value("${asap.sources.off.base-url:https://world.openfoodfacts.org}") String offUrl,
            @Value("${asap.sources.upcitemdb.base-url:https://api.upcitemdb.com}") String upcUrl,
            @Value("${asap.sources.upcitemdb.daily-quota:90}") int upcQuota,
            @Value("${asap.sources.upcitemdb.burst:5}") int upcBurst,
            @Value("${asap.sources.per-source-timeout:8s}") Duration perSource,
            @Value("${asap.sources.total-budget:12s}") Duration budget) {
        List<ProductSource> sources = enabled
                ? List.of(new OpenFactsSource(http, offUrl),
                        new UpcItemDbSource(http, upcUrl, upcQuota, upcBurst, Duration.ofMinutes(1), Clock.systemUTC()))
                : List.of();
        return new ProductRouter(sources, budget, perSource, Clock.systemUTC());
    }
}
