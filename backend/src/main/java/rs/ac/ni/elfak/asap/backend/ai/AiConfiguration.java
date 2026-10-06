package rs.ac.ni.elfak.asap.backend.ai;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
