package rs.ac.ni.elfak.asap.backend.sources;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import rs.ac.ni.elfak.asap.backend.sources.SourceTypes.HttpFetcher;
import rs.ac.ni.elfak.asap.backend.sources.SourceTypes.Response;

/** Read-only GET with an identifying User-Agent (required by Open Food Facts), no redirects followed. */
public final class JavaHttpFetcher implements HttpFetcher {

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
    private final String userAgent;

    public JavaHttpFetcher(String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public Response get(String url, Duration timeout) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(timeout)
                .header("User-Agent", userAgent)
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return new Response(response.statusCode(), response.headers().firstValue("Content-Type").orElse(null), response.body());
    }
}
